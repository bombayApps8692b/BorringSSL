// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc
// Source File Name:   ContinueTransaction.java

package com.reader;

import com.mosambee.reader.emv.Command;
import com.mosambee.reader.emv.commands.SolicitedType;

import java.util.Iterator;
import java.util.Map;

// Referenced classes of package com.mosambee.reader.emv.commands:
//            SolicitedType

public class ContinueTransaction extends Command {

    public ContinueTransaction(Map<String, String> map) {
        a = "DE";
        b = "D2";
        c = "00";
        d = "00";
        String s = buildDataField(map);
        e = s;
    }

    private String buildDataField(Map<String, String> map) {
        String s = "";
        Iterator<String> iterator = map.keySet().iterator();
        do {
            if (!iterator.hasNext()) {
                StringBuilder stringbuilder = new StringBuilder("E0");
                String s1 = getHexLength(s);
                return stringbuilder.append(s1).append(s).toString();
            }
            String s2 = (String) iterator.next();
            String s3 = (String) map.get(s2);
            String s4 = String.valueOf(s);
            StringBuilder stringbuilder1 = (new StringBuilder(s4)).append(s2);
            String s5 = getHexLength(s3);
            s = stringbuilder1.append(s5).append(s3).toString();
        } while (true);
    }

    public SolicitedType getType() {
        return SolicitedType.CONTINUE_TRANSCTION;
    }

    public String toString() {
        return "Continue Transaction";
    }

    public static String getHexLength(String paramString) {
        Object[] arrayOfObject = new Object[1];
        int length = Integer.valueOf(Integer.parseInt(new StringBuilder()
                                     .append(paramString.length() / 2).toString(), 10));
        arrayOfObject[0] = Integer.valueOf(Integer.parseInt(new StringBuilder()
                                           .append(paramString.length() / 2).toString(), 10));
        if (length > 128)
            return "81" + String.format("%02X", arrayOfObject);
        else
            return String.format("%02X", arrayOfObject);
    }

}
