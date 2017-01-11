# Alfresco Website CMS AMP, simple website CMS to build websites purely on Alfresco (like https://wbif.eu)

Simple website CMS used to build websites purely on Alfresco (like https://wbif.eu)

### Usage

#### Create AMP
```
mvn clean install
```
#### Install AMP
```
/opt/alfresco/bin/apply_amps.sh
```
or
```
java -jar /opt/alfresco/bin/alfresco-mmt.jar install rs-make-alfresco-website /opt/alfresco/tomcat/webapps/alfresco.war
```

### License
Licensed under the MIT license.
http://www.opensource.org/licenses/mit-license.php
