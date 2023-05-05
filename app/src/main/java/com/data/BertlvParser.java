package com.data;

import java.util.HashMap;
import java.util.Map;

public class BertlvParser {

	static String tlvData = "5F20104D54495030382D3120444D43203133414F08A0000000041010015F24032512319F160F4243544553542031323334353637389F21031101409A031509089F02060000000020009F03060000000000009F34034103029F12104465626974204D6173746572436172649F0607A00000000410105F300202019F4E0F616263640000000000000000000000C408545721FFFFFF0012C00AFFFF9876543210E00005C28201804C968AA6B36D6F144D0D007272EF2A77E3A0DE91183D932413B257B6462A3521E14F4289D57F51D7510DB1105B65245879B0CBCB8A5FB3213E025CEBCA901E8B1ACD52917854B8192DDFBE58835DC88C985D49C11AF86AE487E68BC22B455C3D05AD0088C319702C328690408F025BD314930C0CD9E4DE84BFF06F00165661EDEEA0ED3D51C6D494D20839D78CF6E936066309AF4E67A0D6985DA75F1D98EB13EAF5579AB8ADF536BF6FE32B7C5B3528D4F3DAC41E2C717452FB981D3A958D039C66DDBB4217ABDE8C38EDA20DEAFD8F4FCDD7BC975D4E6C01C61AB3492612A84F6077D9D1361CF81C0BB6EE8C6D307FACB23D3316F3E8BBA51EA501D5C55257F96799AC308C05797CD4D17335472869B9BB186EBD3434AE047AF946066974B2F8AD8DD979007C7A95183285238F3357DD491D05BDA2B43B9C815444FBD487686C084C45A2498D8435359777477110EE93B0F697DDB25DF02637438B84936EF8A30F6770E1C94888A714B8BC4F60247D099272453B582AFE4954A413EDCF0B55";

	public static void main(String[] args) {
		BertlvParser obj = new BertlvParser();
		try {
			obj.parseData(tlvData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isHavingNextByte(String tag) {
		return buildBinary(tag)
				.endsWith("11111");
	}

	private String buildBinary(String data) {
		data=Integer.toBinaryString(Integer.parseInt(data, 16));
		StringBuilder sb = new StringBuilder(data);
		String zero = "0";
		while (sb.length() < 8) {
			sb.insert(0, zero);
		}
		return sb.toString();

	}

	private boolean isHaving3rdByte(String tag) {
		return buildBinary(tag)
				.startsWith("1");
	}

	public Map<String, String> parseData(String data) throws Exception {

		Map<String, String> map = new HashMap<String, String>();
		while (!data.equals("")) {
			String singleTag = getsingleByte(data);
			if (singleTag.equals("00")) {
				data = data.substring(2);
				continue;
			}
			if (isHavingNextByte(singleTag)) {
				singleTag += getsingleByte(data.substring(2));
				if (isHaving3rdByte(singleTag.substring(2)))
					singleTag += getsingleByte(data.substring(4));
			}

			data = data.substring(singleTag.length());
            if(data==null||data.equals("")||data.length()<2||data.equals("00")){
                TRACE.i("******************************************Parsing Fail*****************************************");
                continue;
            }
			int num = Integer.parseInt(data.substring(0, 2), 16) * 2;
			if (num > data.length() - 2){
				break;
				}
			data = data.substring(2);
			String str = data.substring(0, num);
			data = data.substring(num);
			// System.out.println(data);
			// System.out.println("<Tag>"+singleTag+"<value>"+str);
			map.put(singleTag, str);

		}
		return map;

	}

	private String getsingleByte(String data) {
		return data.substring(0, 2);

	}
}
