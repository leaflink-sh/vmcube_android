#include "kr_co_lguplus_mucloud_CPCProperty.h"

JNIEXPORT jstring JNICALL Java_kr_co_lguplus_mucloud_CPCProperty_getUrl
(JNIEnv * env, jobject obj){

//	jstring ret = "https://nmucloud.lguplus.co.kr";
	jstring ret = "https://mucloud.lguplus.co.kr";
    //jstring ret = "http://devmucloud.lguplus.co.kr";

	return (*env)->NewStringUTF(env, ret);
}

JNIEXPORT jstring JNICALL Java_kr_co_lguplus_mucloud_CPCProperty_getHost
(JNIEnv * env, jobject obj, jint val){

	jstring ret = "";

    /* (Operator) */
    if(val == 1)
        ret = "/vmCubeMobile/Login/ChangePassword";
    else if(val == 2)
        ret = "/vmCubeMobile/Deploy/RequiredAppGuide";
    else if(val == 3)
        ret = "/vmCubeMobile/Board/LicensePolicyList";
    else if(val == 4)
        ret = "/vmCubeMobile/Logout";
    else if(val == 5)
        ret = "/vmCubeMobile";
    else if(val == 6)
        ret = "/vmCubeMobile/login";
    else if(val == 7)
        ret = "/vmCubeMobile/login/appinit";
    else if(val == 8)
        ret = "/vmCubeMobile/Management/Request/DeviceUse";
    else
        ret = "";

    /* (Test)
	if(val == 1)
		ret = "/vmCubeMobileDev/Login/ChangePassword";
	else if(val == 2)
		ret = "/vmCubeMobileDev/Deploy/RequiredAppGuide";
	else if(val == 3)
		ret = "/vmCubeMobileDev/Board/LicensePolicyList";
	else if(val == 4)
		ret = "/vmCubeMobileDev/Logout";
	else if(val == 5)
		ret = "/vmCubeMobileDev";
	else if(val == 6)
		ret = "/vmCubeMobileDev/login";
	else if(val == 7)
		ret = "/vmCubeMobileDev/login/appinit";
	else if(val == 8)
		ret = "/vmCubeMobileDev/Management/Request/DeviceUse";
	else
		ret = "";*/


	return (*env)->NewStringUTF(env, ret);
}

JNIEXPORT jstring JNICALL Java_kr_co_lguplus_mucloud_CPCProperty_getCustCd
(JNIEnv * env, jobject obj){

	//return (*env)->NewStringUTF(env, "UCLOUD");
	return (*env)->NewStringUTF(env, "VMCUBE");
}

JNIEXPORT jstring JNICALL Java_kr_co_lguplus_mucloud_CPCProperty_getInterfaceUrl
(JNIEnv * env, jobject obj, jint val){

	jstring ret = "";

    /* (Operator)*/

//    if(val == 1)	// OI_VMC_01
//        ret = "https://nex-iucloud.lguplus.co.kr/vmCubeMobileInf/OIService.svc/OI_VMC_01";
//    else if(val == 2) // OI_VMC_02
//        ret = "https://nex-iucloud.lguplus.co.kr/vmCubeMobileInf/OIService.svc/OI_VMC_02";
//    else if(val == 3) // OI_VMC_03
//        ret = "https://nex-iucloud.lguplus.co.kr/vmCubeMobileInf/OIService.svc/OI_VMC_03";
//    else if(val == 4) // OI_VMC_04
//        ret = "https://nex-iucloud.lguplus.co.kr/vmCubeMobileInf/OIService.svc/OI_VMC_04";
//    else if(val == 5) // OI_VMC_05
//        ret = "https://nex-iucloud.lguplus.co.kr/vmCubeMobileInf/OIService.svc/OI_VMC_05";
//    else
//        ret = "";

    if(val == 1)	// OI_VMC_01
        ret = "https://ext-iucloud.lguplus.co.kr/vmCubeMobileInf/OIService.svc/OI_VMC_01";
    else if(val == 2) // OI_VMC_02
        ret = "https://ext-iucloud.lguplus.co.kr/vmCubeMobileInf/OIService.svc/OI_VMC_02";
    else if(val == 3) // OI_VMC_03
        ret = "https://ext-iucloud.lguplus.co.kr/vmCubeMobileInf/OIService.svc/OI_VMC_03";
    else if(val == 4) // OI_VMC_04
        ret = "https://ext-iucloud.lguplus.co.kr/vmCubeMobileInf/OIService.svc/OI_VMC_04";
    else if(val == 5) // OI_VMC_05
        ret = "https://ext-iucloud.lguplus.co.kr/vmCubeMobileInf/OIService.svc/OI_VMC_05";
    else
        ret = "";

    /* (Test)
	if(val == 1)	// OI_VMC_01
		ret = "http://devext-iucloud.lguplus.co.kr/vmCubeMobileInfDev/OIService.svc/OI_VMC_01";
	else if(val == 2) // OI_VMC_02
		ret = "http://devext-iucloud.lguplus.co.kr/vmCubeMobileInfDev/OIService.svc/OI_VMC_02";
	else if(val == 3) // OI_VMC_03
		ret = "http://devext-iucloud.lguplus.co.kr/vmCubeMobileInfDev/OIService.svc/OI_VMC_03";
	else if(val == 4) // OI_VMC_04
		ret = "http://devext-iucloud.lguplus.co.kr/vmCubeMobileInfDev/OIService.svc/OI_VMC_04";
	else if(val == 5) // OI_VMC_05
		ret = "http://devext-iucloud.lguplus.co.kr/vmCubeMobileInfDev/OIService.svc/OI_VMC_05";
	else
		ret = "";
    */

	return (*env)->NewStringUTF(env, ret);
}


JNIEXPORT jstring JNICALL Java_kr_co_lguplus_mucloud_CPCProperty_getKey
(JNIEnv * env, jobject obj){

	//jstring ret = "ud3gUxZLos3xuHMZ0PEa5Wjlt9PT59eg";
	jstring ret = "";
	return (*env)->NewStringUTF(env, ret);
}