# 1번째 스테이지 -> 빌드 영역
# 베이스 이미지로 openjdk:17 버전을 사용하겠다
# 이밎 빌드 시 java 17버전이 설치된 리눅스 환경을 깔아라
FROM eclipse-temurin:17-jdk-alpine AS build1

#작업 폴더 생성 (이제부터 컨테이너 안의 /app 이라는 폴더에서 작업할게!)
WORKDIR /app
# 이 dockerfile을 기준으로 현재 경로에 있는 모든 파일을 app으로 복사하겠다. (소스코드, gradle 등등)

COPY . .

# chmod를 통해서 gradle wrapper 파일을 실행할 수 있는 권한을 줘야 한다.
# 컨테이너 기준에서는 gradlew가 외부 파일이기 때문에 실행할 수 있는 권한이 없다.
RUN chmod +x ./gradlew
# gradlew에 기존에 있었던 건 (clean) 하고 새롭게 빌드해
# 개발 과정에서는 시간 단축 등을 위해 test를 생략하고 빌드하는 것도 가능 (-x)
Run ./gradlew clean build -x test

##########################################################################

## 2번째 스테이지 -> 실행 영역
From eclipse-temurin:17-jre-alpine

# build 라는 별칭으로 만들어진 첫번째 스테이지에서
# .jar로 끝나는 파일을 app.jar로 복사해서 이미지에 세팅
COPY --from=build1 /app/build/libs/*.jar app.jar

# 이 컨테이너가 시작될 때 무조건 실행해야 하는 명령어
ENTRYPOINT ["java" , "-jar" , "app.jar"]

# CMD는 기본 실행 명령어를 의미. 컨테이너 실행 시에 다른 명령어가 주어지면 그 명령어로 대체됨.
# ENTRYPOINT는 반드시 실행되어야 할 명령어를 의미. 다른 명령어로 대체되지 않음.
# 스프링 부트는 무조건 -jar 옵션으로 실행되어야 하기에 ENTRYPOINT로 안전하게 선언.

ENV TZ=Asia/Seoul
RUN apk add --no-cache curl tzdata
