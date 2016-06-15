## PackageWeb
package rpm,deb,tar on web page

###Feature
- support upload file type tar,tar.gz,zip
- not focus on Linux versions
- convenience through web

###Assumptions
- must install fpm before run. Reference: https://github.com/jordansissel/fpm

###How to run
mvn spring-boot:run

###Web Page
![Image](../master/screenshots/packageweb1.png?raw=true)
![Image](../master/screenshots/packageweb2.png?raw=true)
![Image](../master/screenshots/filedownload.png?raw=true)

###Stack
- Spring boot
- thymeleaf
- fpm




