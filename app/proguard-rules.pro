# Proguard config for the demo project.
#
#

# Most of the following options are standard config from ADT.
# See <sdk>/tools/proguard/examples/android.pro for details.
# Additional options required by the Google Maps Android API v2
# are commented where necessary.

-dontpreverify
-flattenpackagehierarchy
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses

-verbose

# We restrict a few more optimizations for the maps library.
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/simplification/variable

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.app.Fragment
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.support.v7.widget




-keep public class ndrnnustculsflw.**{*;}
-keep public class com.paycore.l2kernel.**{*;}



#-dump proguard/class_files.txt
#未混淆的类和成员
-printseeds proguard/seeds.txt
#列出从 apk 中删除的代码
-printusage proguard/unused.txt
#混淆前后的映射
-printmapping proguard/mapping.txt
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class org.apache.** {
  public protected *;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


#-libraryjars "D:\CPOCAPP\MosambeeModularThemeV2\MosambeeModularThemeV2_CPOC\app\libs\classes.jar"
#-libraryjars "D:\CPOCAPP\MosambeeModularThemeV2\MosambeeModularThemeV2_CPOC\app\libs\dspread_android_pos_sdk_2.9.9_202004271245.jar"
#-libraryjars "D:\CPOCAPP\MosambeeModularThemeV2\MosambeeModularThemeV2_CPOC\app\libs\mosambee-miura.jar"
#-libraryjars "D:\CPOCAPP\MosambeeModularThemeV2\MosambeeModularThemeV2_CPOC\app\libs\MposApiLib_Android_V1.00.01_20141210T.jar"
#-libraryjars "D:\CPOCAPP\MosambeeModularThemeV2\MosambeeModularThemeV2_CPOC\app\libs\org.apache.http.legacy.jar"
#-libraryjars "D:\CPOCAPP\MosambeeModularThemeV2\MosambeeModularThemeV2_CPOC\app\libs\printersdk.jar"

#(classes.jar;dspread_android_pos_sdk_2.9.9_202004271245.jar;mosambee-miura.jar;MposApiLib_Android_V1.00.01_20141210T.jar;org.apache.http.legacy.jar;printersdk.jar;)


# As described in tools/proguard/examples/android.pro - ignore all warnings.
-dontwarn android.support.v4.**
-dontwarn com.squareup.okhttp3.**
-dontwarn com.dspread.xpos.**
-dontwarn okio.**
-dontwarn android.support.v7.**
-dontwarn org.bouncycastle.**



# The maps library uses custom Parcelables.  Use this rule (which is slightly
# broader than the standard recommended one) to avoid obfuscating them.
-keepclassmembers class * implements android.os.Parcelable {
    static *** CREATOR;
}

# The maps library uses serialization so keep this.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Additional ones needed for Maps API library.
-keep public class com.google.googlenav.capabilities.CapabilitiesController*

# Additional config needed for Guava which is used by the demo.
-keep public interface com.google.common.base.FinalizableReference {
    void finalizeReferent();
}
# Missing annotations are harmless.
-dontwarn sun.misc.Unsafe
-dontwarn javax.annotation.**
-dontwarn android.net.**
-dontwarn org.apache.http.**


# Ignore invalid constant ref.  See
# https://groups.google.com/d/topic/guava-discuss/YCZzeCiIVoI/discussion
-dontwarn com.google.common.collect.MinMaxPriorityQueue
