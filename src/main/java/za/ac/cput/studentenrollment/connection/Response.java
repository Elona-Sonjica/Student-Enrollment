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
public class Response implements Serializable {
    private ResponseStatus status;
    private Object data;
    private String message;

    public Response(ResponseStatus status, Object data) {
        this.status = status;
        this.data = data;
        this.message = null;
    }

    public Response(ResponseStatus status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public Response(ResponseStatus status, Object data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public ResponseStatus getStatus() { return status; }
    public void setStatus(ResponseStatus status) { this.status = status; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isSuccess() {
        return status == ResponseStatus.SUCCESS;
    }
    
    @Override
    public String toString() {
        return "Response{status=" + status + ", message=" + message + ", data=" + data + "}";
    }
}