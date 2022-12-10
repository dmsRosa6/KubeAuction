FROM node:latest
WORKDIR /usr/app
COPY . /usr/app
RUN rm -rf node_modules &&\
	npm install &&\
	npm install -g artillery &&\
	npm install --save-dev @faker-js/faker &&\
	npm install -g node-fetch -save &&\
	npm install -g https://github.com/preguica/artillery-plugin-metrics-by-endpoint.git
CMD artillery run test-images.yml &&\
	artillery run create-users.yml &&\
	artillery run create-auctions.yml &&\
	artillery run workload1.yml
	