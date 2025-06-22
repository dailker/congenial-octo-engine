# Tasky - Heroku Setup Instructions

When deploying to Heroku, you must provide the following configuration variables:

- **MONGODB_URI**: Your MongoDB Atlas connection string (e.g. `mongodb+srv://user:password@cluster.mongodb.net/dbname?retryWrites=true&w=majority`)
- **JWT_SECRET**: A secure secret key for JWT signing (e.g. a long random string)
- **ADMIN_USER**: (Optional) Admin username for Spring Security
- **ADMIN_PASSWORD**: (Optional) Admin password for Spring Security

You can set these in the Heroku dashboard under **Settings > Config Vars** or via the Heroku CLI:

```sh
heroku config:set MONGODB_URI=your_mongodb_uri
heroku config:set JWT_SECRET=your_jwt_secret
heroku config:set ADMIN_USER=admin
heroku config:set ADMIN_PASSWORD=your_admin_password
```

**Questions to ask during setup/clone:**
- What is your MongoDB Atlas connection string? (MONGODB_URI)
- What is your JWT secret key? (JWT_SECRET)
- What admin username do you want? (ADMIN_USER)
- What admin password do you want? (ADMIN_PASSWORD)

**To run locally with Heroku-style config:**
```sh
export MONGODB_URI=your_mongodb_uri
export JWT_SECRET=your_jwt_secret
export ADMIN_USER=admin
export ADMIN_PASSWORD=your_admin_password
./mvnw spring-boot:run
```

