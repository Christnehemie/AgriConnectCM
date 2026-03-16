@echo off
set ROOT=%~dp0

echo ============================================
echo   AGRICONNECT - BUILD AND DEPLOY
echo ============================================

echo.
echo [1/3] Build Maven...

cd %ROOT%backend\service-registry
call mvn clean package -DskipTests
echo [OK] service-registry

cd %ROOT%backend\api-gateway
call mvn clean package -DskipTests
echo [OK] api-gateway

cd %ROOT%backend\service-utilisateurs
call mvn clean package -DskipTests
echo [OK] service-utilisateurs

cd %ROOT%backend\service-abonnements
call mvn clean package -DskipTests
echo [OK] service-abonnements

echo.
echo [2/3] Docker...
cd %ROOT%devOps
docker-compose down
docker-compose build
docker-compose up -d

echo.
echo ============================================
echo   DEPLOY TERMINE
echo ============================================
echo   Service Registry : http://localhost:8761
echo   API Gateway      : http://localhost:8080
echo   Utilisateurs     : http://localhost:8081
echo   Abonnements      : http://localhost:8082
echo   Frontend         : http://localhost:4200
echo ============================================
pause