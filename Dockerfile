FROM java:8
MAINTAINER eugenegoldberg@discover.com
VOLUME /tmp
EXPOSE 8080
 
ENV USER_NAME eugene
ENV APP_HOME /home/$USER_NAME/app
 
RUN useradd -ms /bin/bash $USER_NAME
RUN mkdir $APP_HOME
 
ADD target/catalog-api-0.0.1-SNAPSHOT.jar $APP_HOME/target/catalog-api-0.0.1-SNAPSHOT.jar
RUN chown $USER_NAME $APP_HOME/target/catalog-api-0.0.1-SNAPSHOT.jar
 
USER $USER_NAME
WORKDIR $APP_HOME
RUN bash -c 'touch target/catalog-api-0.0.1-SNAPSHOT.jar'
 
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","target/catalog-api-0.0.1-SNAPSHOT.jar"]
