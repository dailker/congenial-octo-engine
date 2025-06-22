# Tasky - Heroku Setup Instructions

When deploying to Heroku, you can use the **Deploy to Heroku** button or the Heroku CLI.  
This project includes an `app.json` file, which will prompt you for configuration values during deployment.

## One-Click Deploy

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.svg)](https://www.heroku.com/deploy?template=https://github.com/dailker/Tasky)

- Click the button above.
- Heroku will prompt you for:
  - **MONGODB_URI**: Your MongoDB Atlas connection string (must include a database name)
  - **JWT_SECRET**: Your JWT secret key
  - **ADMIN_USER**: Admin username for Spring Security (default: admin)
  - **ADMIN_PASSWORD**: Admin password for Spring Security (default: admin)

## Manual Heroku CLI Setup

You can also set config vars manually:

```sh
heroku config:set MONGODB_URI=your_mongodb_uri
heroku config:set JWT_SECRET=your_jwt_secret
heroku config:set ADMIN_USER=admin
heroku config:set ADMIN_PASSWORD=your_admin_password
```

**Questions you will be prompted for during setup:**
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

---

**Terms of Use:**  
By using the Deploy to Heroku button, you agree to the [Heroku Terms of Use](https://www.heroku.com/policy/tos) unless you provide your own Terms of Use or license file in this repository.

**Important:**  
All sensitive values are now set via environment variables.  
If you are sharing or committing this project, **do not hardcode secrets in `src/main/resources/application.properties`**.  
**Never commit production secrets to public repositories.**

