package poc;

public interface NgRequestPtr {
	
	/*
ngx_http_send_response(ngx_http_request_t *r, ngx_uint_t status,
    ngx_str_t *ct, ngx_http_complex_value_t *cv)
	 */
	void sendResponse(int status, String contentType, String content);
	int getSignature();
	

}
