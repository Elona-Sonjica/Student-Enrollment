/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.connection;

import java.io.Serializable;
/**
 *
 * @author elzas
 */
public class Request implements Serializable {
    private RequestType type;
    private Object data;

    public Request(RequestType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public Request(RequestType type) {
        this.type = type;
        this.data = null;
    }

    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    @Override
    public String toString() {
        return "Request{type=" + type + ", data=" + data + "}";
    }
}