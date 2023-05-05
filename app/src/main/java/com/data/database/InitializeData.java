package com.data.database;

public class InitializeData {
    int id=0;
    String pan_h,  pan_l, pan_le, pan_ty, pan_pin, pan_tip, op1, op2;

    public InitializeData( String pan_h, String pan_l,String pan_le,String pan_ty,String pan_pin,String pan_tip,String op1,String op2) {
        this.pan_h=pan_h;
        this.pan_l=pan_l;
        this.pan_le=pan_le;
        this.pan_ty=pan_ty;
        this.pan_pin=pan_pin;
        this.pan_tip=pan_tip;
        this.op1=op2;
        this.op2=op2;
    }

    public InitializeData() {
        // TODO Auto-generated constructor stub
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPan_h() {
        return pan_h;
    }

    public void setPan_h(String pan_h) {
        this.pan_h = pan_h;
    }

    public String getPan_l() {
        return pan_l;
    }

    public void setPan_l(String pan_l) {
        this.pan_l = pan_l;
    }

    public String getPan_le() {
        return pan_le;
    }

    public void setPan_le(String pan_le) {
        this.pan_le = pan_le;
    }

    public String getPan_ty() {
        return pan_ty;
    }

    public void setPan_ty(String pan_ty) {
        this.pan_ty = pan_ty;
    }

    public String getPan_pin() {
        return pan_pin;
    }

    public void setPan_pin(String pan_pin) {
        this.pan_pin = pan_pin;
    }

    public String getPan_tip() {
        return pan_tip;
    }

    public void setPan_tip(String pan_tip) {
        this.pan_tip = pan_tip;
    }

    public String getOp1() {
        return op1;
    }

    public void setOp1(String op1) {
        this.op1 = op1;
    }

    public String getOp2() {
        return op2;
    }

    public void setOp2(String op2) {
        this.op2 = op2;
    }


}
