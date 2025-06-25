#!/usr/bin/env bash

set -eo pipefail

MODULES=("notifications" "api")
NAMESPACE="kubeauction"
TAG="latest"

CLUSTER_NAME="kubeauction-cluster"
KIND_CONFIG="kind-config.yaml"
NS_FILE="namespace.yaml"
NAMESPACE="kubeauction"

RES_FOLDERS=(mongo redis minio app)

function info  { echo -e "\e[34m[INFO]\e[0m  $*"; }

function error { echo -e "\e[31m[ERROR]\e[0m $*" >&2; exit 1; }

cd Kubernetes/

if ! kind get clusters | grep -qx "$CLUSTER_NAME"; then
  info "Creating kind cluster '$CLUSTER_NAME'…"
  kind create cluster --name "$CLUSTER_NAME" --config "$KIND_CONFIG"
else
  info "Kind cluster '$CLUSTER_NAME' already exists"
fi

cd ..

cd KubeAuction

mvn clean package -DskipTests

cd ..

for module in "${MODULES[@]}"; do
    echo "Building image: $NAMESPACE-$module:$TAG"
    docker build -t "$NAMESPACE-$module:$TAG" "./KubeAuction/$module"
    kind load docker-image "$NAMESPACE-$module:$TAG" \
    --name "$CLUSTER_NAME"
done

cd Kubernetes/


kubectl cluster-info --context "kind-$CLUSTER_NAME" &>/dev/null \
  || error "Could not switch context to kind-$CLUSTER_NAME"

info "Applying namespace manifest…"
kubectl apply -f "$NS_FILE"

for folder in "${RES_FOLDERS[@]}"; do
  if [[ -d "$folder" ]]; then
    info "Applying all manifests in ./$folder to namespace '$NAMESPACE'…"
    kubectl apply -n "$NAMESPACE" -f "./$folder"
  else
    error "Expected folder ./$folder not found"
  fi
done

info "Applying top-level ingress.yaml…"
kubectl apply -n "$NAMESPACE" -f ingress.yaml

echo
info "All resources applied!"
info "To tear down everything, run: kind delete cluster --name $CLUSTER_NAME"

kubectl port-forward service/kubeauction-app 8080:8080 -n kubeauction


