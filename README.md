#Huong dan setup backend
Java 21
Jdk 21

Cau hinh IDE code javapsring
Settings → Build, Execution, Deployment → Compiler → Annotation Processors(Ho tro loomnbook)



Tải docker, dán vào terminal


set up docker de chay postgreSQL

Tải image PostgreSQL mới nhất
docker pull postgres:latest

Chay db = cau lenh
docker run -d --name my-postgres -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=c2vn -e POSTGRES_DB=lmsdb -p 5432:5432 -v pgdata:/var/lib/postgresql/data postgres:latest


set up docker de chay redis

Tải image redis mới nhất
docker pull redis:latest

chay redis = cau lenh
docker run -d --name my-redis -p 6379:6379 redis:latest