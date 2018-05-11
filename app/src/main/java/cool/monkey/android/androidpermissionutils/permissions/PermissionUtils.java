package cool.monkey.android.androidpermissionutils.permissions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cool.monkey.android.androidpermissionutils.CCApplication;

public class PermissionUtils {

    private static final List<String> PERMISSIONS = getPermissions();
    public static PermissionUtils sInstance;
    public PermissionUtils.OnRationaleListener mOnRationaleListener;
    public PermissionUtils.SimpleCallback mSimpleCallback;
    public PermissionUtils.FullCallback mFullCallback;
    public PermissionUtils.ThemeCallback mThemeCallback;
    public Set<String> mPermissions = new LinkedHashSet();
    public List<String> mPermissionsRequest;
    public List<String> mPermissionsGranted;
    public List<String> mPermissionsDenied;
    public List<String> mPermissionsDeniedForever;

    public static List<String> getPermissions() {
        return getPermissions(CCApplication.getInstance().getPackageName());
    }

    @SuppressLint("WrongConstant")
    public static List<String> getPermissions(String var0) {
        PackageManager var1 = CCApplication.getInstance().getPackageManager();
        try {
            return Arrays.asList(var1.getPackageInfo(var0, 4096).requestedPermissions);
        } catch (PackageManager.NameNotFoundException var3) {
            var3.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static boolean isGranted(String... var0) {
        String[] var1 = var0;
        int var2 = var0.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            String var4 = var1[var3];
            if (!isGranted(var4)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isGranted(String var0) {
        return Build.VERSION.SDK_INT < 23 || 0 == ContextCompat.checkSelfPermission(CCApplication.getInstance(), var0);
    }

    @SuppressLint("WrongConstant")
    public static void launchAppDetailsSettings() {
        Intent var0 = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        var0.setData(Uri.parse("package:" + CCApplication.getInstance().getPackageName()));
        CCApplication.getInstance().startActivity(var0.addFlags(268435456));
    }

    public static PermissionUtils permission(String... var0) {
        return new PermissionUtils(var0);
    }

    private PermissionUtils(String... var1) {
        String[] var2 = var1;
        int var3 = var1.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String var5 = var2[var4];
            String[] var6 = PermissionConstants.getPermissions(var5);
            int var7 = var6.length;

            for (int var8 = 0; var8 < var7; ++var8) {
                String var9 = var6[var8];
                if (PERMISSIONS.contains(var9)) {
                    this.mPermissions.add(var9);
                }
            }
        }

        sInstance = this;
    }

    public PermissionUtils rationale(PermissionUtils.OnRationaleListener var1) {
        this.mOnRationaleListener = var1;
        return this;
    }

    public PermissionUtils callback(PermissionUtils.SimpleCallback var1) {
        this.mSimpleCallback = var1;
        return this;
    }

    public PermissionUtils callback(PermissionUtils.FullCallback var1) {
        this.mFullCallback = var1;
        return this;
    }

    public PermissionUtils theme(PermissionUtils.ThemeCallback var1) {
        this.mThemeCallback = var1;
        return this;
    }

    public void request() {
        this.mPermissionsGranted = new ArrayList();
        this.mPermissionsRequest = new ArrayList();
        if (Build.VERSION.SDK_INT < 23) {
            this.mPermissionsGranted.addAll(this.mPermissions);
            this.requestCallback();
        } else {
            Iterator var1 = this.mPermissions.iterator();

            while (var1.hasNext()) {
                String var2 = (String) var1.next();
                if (isGranted(var2)) {
                    this.mPermissionsGranted.add(var2);
                } else {
                    this.mPermissionsRequest.add(var2);
                }
            }

            if (this.mPermissionsRequest.isEmpty()) {
                this.requestCallback();
            } else {
                this.startPermissionActivity();
            }
        }

    }

    @RequiresApi(
            api = 23
    )
    private void startPermissionActivity() {
        this.mPermissionsDenied = new ArrayList();
        this.mPermissionsDeniedForever = new ArrayList();
        PermissionActivity.start(CCApplication.getInstance());
    }

    @RequiresApi(
            api = 23
    )
    private boolean rationale(Activity var1) {
        boolean var2 = false;
        if (this.mOnRationaleListener != null) {
            Iterator var3 = this.mPermissionsRequest.iterator();

            while (var3.hasNext()) {
                String var4 = (String) var3.next();
                if (var1.shouldShowRequestPermissionRationale(var4)) {
                    this.getPermissionsStatus(var1);
                    this.mOnRationaleListener.rationale(new PermissionUtils.OnRationaleListener.ShouldRequest() {
                        @Override
                        public void again(boolean var1) {
                            if (var1) {
                                startPermissionActivity();
                            } else {
                                requestCallback();
                            }

                        }
                    });
                    var2 = true;
                    break;
                }
            }
            this.mOnRationaleListener = null;
        }
        return var2;
    }

    private void getPermissionsStatus(Activity var1) {
        Iterator var2 = this.mPermissionsRequest.iterator();

        while (var2.hasNext()) {
            String var3 = (String) var2.next();
            if (isGranted(var3)) {
                this.mPermissionsGranted.add(var3);
            } else {
                this.mPermissionsDenied.add(var3);
                if (!var1.shouldShowRequestPermissionRationale(var3)) {
                    this.mPermissionsDeniedForever.add(var3);
                }
            }
        }
    }

    private void requestCallback() {
        if (this.mSimpleCallback != null) {
            if (this.mPermissionsRequest.size() != 0 && this.mPermissions.size() != this.mPermissionsGranted.size()) {
                if (!this.mPermissionsDenied.isEmpty()) {
                    this.mSimpleCallback.onDenied();
                }
            } else {
                this.mSimpleCallback.onGranted();
            }

            this.mSimpleCallback = null;
        }

        if (this.mFullCallback != null) {
            if (this.mPermissionsRequest.size() != 0 && this.mPermissions.size() != this.mPermissionsGranted.size()) {
                if (!this.mPermissionsDenied.isEmpty()) {
                    this.mFullCallback.onDenied(this.mPermissionsDeniedForever, this.mPermissionsDenied);
                }
            } else {
                this.mFullCallback.onGranted(this.mPermissionsGranted);
            }

            this.mFullCallback = null;
        }

        this.mOnRationaleListener = null;
        this.mThemeCallback = null;
    }

    public void onRequestPermissionsResult(Activity var1) {
        this.getPermissionsStatus(var1);
        this.requestCallback();
    }

    public interface ThemeCallback {
        void onActivityCreate(Activity var1);
    }

    public interface FullCallback {
        void onGranted(List<String> var1);

        void onDenied(List<String> var1, List<String> var2);
    }

    public interface SimpleCallback {
        void onGranted();

        void onDenied();
    }

    public interface OnRationaleListener {
        void rationale(PermissionUtils.OnRationaleListener.ShouldRequest var1);

        public interface ShouldRequest {
            void again(boolean var1);
        }
    }

    @RequiresApi(
            api = 23
    )
    public static class PermissionActivity extends Activity {
        public PermissionActivity() {
        }

        @SuppressLint("WrongConstant")
        public static void start(Context var0) {
            Intent var1 = new Intent(var0, PermissionActivity.class);
            var1.addFlags(268435456);
            var0.startActivity(var1);
        }

        @Override
        protected void onCreate(@Nullable Bundle var1) {
            if (PermissionUtils.sInstance == null) {
                super.onCreate(var1);
                Log.e("PermissionUtils", "request permissions failed");
                this.finish();
            } else {
                if (PermissionUtils.sInstance.mThemeCallback != null) {
                    PermissionUtils.sInstance.mThemeCallback.onActivityCreate(this);
                }

                super.onCreate(var1);
                if (PermissionUtils.sInstance.rationale((Activity) this)) {
                    this.finish();
                } else {
                    if (PermissionUtils.sInstance.mPermissionsRequest != null) {
                        int var2 = PermissionUtils.sInstance.mPermissionsRequest.size();
                        if (var2 <= 0) {
                            this.finish();
                            return;
                        }

                        this.requestPermissions((String[]) PermissionUtils.sInstance.mPermissionsRequest.toArray(new String[var2]), 1);
                    }

                }
            }
        }

        @Override
        public void onRequestPermissionsResult(int var1, @NonNull String[] var2, @NonNull int[] var3) {
            if (var2 == null) {
                throw new NullPointerException("Argument 'permissions' of type String[] (#1 out of 3, zero-based) is marked by @android.support.annotation.NonNull but got null for it");
            } else if (var3 == null) {
                throw new NullPointerException("Argument 'grantResults' of type int[] (#2 out of 3, zero-based) is marked by @android.support.annotation.NonNull but got null for it");
            } else {
                PermissionUtils.sInstance.onRequestPermissionsResult(this);
                this.finish();
            }
        }
    }
}
