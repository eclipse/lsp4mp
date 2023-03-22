package org.acme.hibernate.orm;

import javax.ws.rs.Path;

@Path("myPath")
public class CustomJaxRsResolving {

    public String myMethod() {
        return "my string";
    }

}