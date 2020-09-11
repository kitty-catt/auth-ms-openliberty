package dev.appsody.auth;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/resource")
public class AuthResource {

    @GET
    public String getRequest() {
        return "AuthResource response";
    }
}
