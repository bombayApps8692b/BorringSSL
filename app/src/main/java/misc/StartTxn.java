package misc;


public enum StartTxn {
	EMV_OK(0,"Succeed"),
    EMV_DENIAL(1,"Transaction denied"),
    EMV_DATA_ERR(2,"IC card data format error"),
    EMV_NOT_ACCEPT(3,"Transaction is not accepted"),
    ICC_CMD_ERR(4,"IC card command failed"),
    ICC_RSP_6985(5,"ICC response 6985 in 1st GAC"),
    EMV_RSP_ERR(6,"IC card response code error"),
    EMV_PARAM_ERR(7,"Parameter error"),
	UNKOWN(-1,"Communication error");
	int id;
	String name;
	private StartTxn(int id ,String name){
		 this.id = id;
	     this.name = name;
	}
	

 public int getId() {
     return id;
 }

 public String getName() {
     return name;
 }
}
