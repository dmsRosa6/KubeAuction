FROM docker.io/tomcat:10-jdk17-openjdk
WORKDIR /usr/local/tomcat/webapps
COPY target/scc2223-project-1.0.war ROOT.war
EXPOSE 8080