package com.activity;


import com.mosambee.mpos.cpoc.R;

public enum FragmentType {
    HAND_SHAKE,
    SCRP_HAND_SHAKE,
    SCRP_PIN,
    LOGIN,
    EMAIL,
    SMS,
    EMI_ENQUIRY(16, R.string.activity_emi_enquiry, "00"),
    EMI_HDFC_ENQUIRY(15, R.string.activity_emi_enquiry, "50"),
    EMI_SALE(15, R.string.activity_emi_sale, "00"),
    FETCH_LOCAL_BIN,
    EMI_PROGRAM_ENQUIRY(18, R.string.activity_emi_program_enquiry, "00"),
    RESET_PIN,
    NON_CARD_TXN(-2,R.string.activity_non_card_txn,"-1"),
    SALE(1, R.string.activity_sale, "00"),
    VOID(2, R.string.activity_void, "02"),
    SETTLEMENT(3, R.string.activity_settlement, ""),
    SETTLEMENT_SUMMARY,
    REFUND(4, R.string.activity_refund, "20"),
    VOID_LIST(5, R.string.activity_void_list,"05"),
    HISTORY(6, R.string.activity_history, "06"),
    SALE_COMPLETE_LIST(7, R.string.sale_complete, "07"),
    SALE_TIP(27,R.string.activity_sale_tip,"27"),
    PRE_AUTH(8, R.string.activity_preAuth, "00"),
    SALE_COMPLETE(9, R.string.activity_sale_complete, "00"),
    CASH(10,R.string.activity_cash,"-1"),
    CHEQUE(11,R.string.activity_cheque,"-1"),
    PWCB(13,R.string.activity_pwcb,"09"), //purchase with cashback
    CBWP(14,R.string.activity_cbwp,"01"), //cashback without purchase
    RECEIPT(15,R.string.activity_receipt,"100"),
    CAPTURE_SIGNATURE(16, R.string.activity_capature_signature, "101"),
    ABOUT(17, R.string.activity_about, "102"),
    CASH_CHEQUE(18, R.string.cash_cheque, "103"),
    PAY_BY_LINK(19, R.string.activity_pay_by_link, "19"),
    WALLET_HISTORY(25,R.string.activity_wallet_history,"25"),
    PAY_BY_LINK_HISTORY(20, R.string.activity_pay_by_link, "20"),
    WALLET(21,R.string.activity_wallet,"21"),
    GET_OTP,
    DEBIT_WALLET,
    GET_QR_CODE(22,R.string.activity_GET_QR_CODE,""),
    UPI_CHECK(22,R.string.activity_UPI_CHECK,""),
    UPI_COLLECT(22,R.string.activity_UPI_COLLECT,""),
    UPI_TXN_STATUS(22,R.string.activity_UPI_TXN_STATUS,""),
    UPI_TXN_HISTORY(22,R.string.activity_UPI_TXN_STATUS,""),
    UPI_REFUND(22,R.string.activity_UPI_TXN_REFUND,""),
    UPI_OFFLINE_REFUND(22,R.string.activity_UPI_OFFLINE_TXN_REFUND,""),
    UPI_PAY_BY_LINK(22,R.string.activity_UPI_PAY_BY_LINK,""),
    EMI(23,R.string.activity_EMI,"23"),
    BHARAT_QR(24,R.string.activity_BHARAT_QR,"24"),
    BHARAT_QR_CHECK_STATUS,
    BHARAT_QR_OFFLINE_REFUND,
    ADHAR(25,R.string.activity_ADHAR,"25"),

    SPOC(26,R.string.activity_SPOC,"26");







    private int id=1;
    private int name;
    private String pcode;

    private FragmentType() {}

    private FragmentType(int id, int name, String pcode) {
        this.id = id;
        this.name = name;
        this.pcode = pcode;
    }

    public int getId() {
        return id;
    }

    public int getName() {
        return name;
    }

    public String getPcode() {
        return pcode;
    }
}