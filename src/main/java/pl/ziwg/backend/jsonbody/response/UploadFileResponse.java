package pl.ziwg.backend.jsonbody.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadFileResponse {
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("file_download_uri")
    private String fileDownloadUri;
    @JsonProperty("file_type")
    private String fileType;
    private long size;

    public UploadFileResponse(String fileName, String fileDownloadUri, String fileType, long size) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
    }


}
