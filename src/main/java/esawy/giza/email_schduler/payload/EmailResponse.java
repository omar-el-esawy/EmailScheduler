package esawy.giza.email_schduler.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Setter
@Getter
public class EmailResponse {

    private boolean success;
    private String jobId;
    private String jobGroup;
    private String message;
    public EmailResponse (boolean success, String message)
    {
        this.success = success;
        this.message = message;
    }


}
