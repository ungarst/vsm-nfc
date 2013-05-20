package se761.bestgroup.vsmreceiver;

import java.io.Serializable;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

public class SerializableCookie implements Serializable {
	 
    private static final long serialVersionUID = 5327445113190674523L; //arbitrary
 
    private String name;
    private String value;
    private String domain;
     
    public SerializableCookie(Cookie cookie){
        this.name = cookie.getName();
        this.value = cookie.getValue();
        this.domain = cookie.getDomain();
    }
     
    private String getName(){
        return name;
    }
     
    private String getValue(){
        return value;
    }
    private String getDomain(){
        return domain;
    }
    
    public Cookie getCookie(){
    	BasicClientCookie2 cookie = new BasicClientCookie2(getName(), getValue());
    	cookie.setDomain(getDomain());
    	return cookie;
    }
}
