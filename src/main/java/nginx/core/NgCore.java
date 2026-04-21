package nginx.core;

public interface NgCore extends NgGlobal {

	int
	  NGX_OK         =  0,
	  NGX_ERROR      = -1,
	  NGX_AGAIN      = -2,
	  NGX_BUSY       = -3,
	  NGX_DONE       = -4,
	  NGX_DECLINED   = -5,
	  NGX_ABORT      = -6;

}
