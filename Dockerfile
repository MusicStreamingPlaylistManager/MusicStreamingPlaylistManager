# Stage 1: Build file .war bằng Maven
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Chạy file .war bằng máy chủ Tomcat 9
FROM tomcat:9.0-jdk8-corretto
WORKDIR /usr/local/tomcat

# Xóa các app mặc định của Tomcat cho nhẹ
RUN rm -rf webapps/*

# Copy file .war vừa build ở bước 1 vào thư mục webapps của Tomcat và đổi tên thành ROOT.war để chạy ở trang chủ (/)
COPY --from=build /app/target/MusicStreamingPlaylistManager-1.0-SNAPSHOT.war webapps/ROOT.war

# Mở cổng 8080
EXPOSE 8080

# Chạy Tomcat
CMD ["catalina.sh", "run"]
