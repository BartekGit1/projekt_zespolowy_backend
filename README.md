# Project Setup and Authentication

## Create Containers
To start the necessary containers, run the following command:
>docker compose up -d
> 

# Keycloak URL:
Once the containers are up and running, you can access Keycloak at:
>http://localhost:8888

# Login request:
> POST: http://localhost:8888/realms/zespolowy/protocol/openid-connect/token
### Request Body Type:
`x-www-form-urlencoded`

### Parameters:
- **client_id**: `zespolowy-app`
- **grant_type**: `password`
- **password**: `<insert password>`
- **username**: `<insert username>`