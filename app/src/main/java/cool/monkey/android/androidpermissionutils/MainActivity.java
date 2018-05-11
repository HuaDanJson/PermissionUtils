package cool.monkey.android.androidpermissionutils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import cool.monkey.android.androidpermissionutils.permissions.PermissionConstants;
import cool.monkey.android.androidpermissionutils.permissions.PermissionUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getGetWritePermission();
    }

    public void getGetWritePermission() {

        PermissionUtils.permission(PermissionConstants.STORAGE, PermissionConstants.CAMERA)
                .rationale(new PermissionUtils.OnRationaleListener() {
                    @Override
                    public void rationale(final ShouldRequest shouldRequest) {
                        shouldRequest.again(true);
                    }
                })
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        ToastHelper.showShortMessage("getPermission Success");
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever,
                                         List<String> permissionsDenied) {
                        if (!permissionsDeniedForever.isEmpty()) {
                            PermissionUtils.launchAppDetailsSettings();
                        }
                    }
                }).request();
    }
}
