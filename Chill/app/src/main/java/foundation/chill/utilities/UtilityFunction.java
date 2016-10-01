package foundation.chill.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import foundation.chill.R;

/**
 * Created by samsiu on 9/22/16.
 */

public class UtilityFunction {

    private static final String TAG = UtilityFunction.class.getSimpleName();
    private static final int FAB_COLOR = R.color.colorAccent;

    public static void sendTweet(Activity activity, String bodyText, Uri editedImageUri){

        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, bodyText);
        tweetIntent.putExtra(Intent.EXTRA_STREAM, editedImageUri);
        tweetIntent.setType("text/plain");

        PackageManager packManager = activity.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent,  PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for(ResolveInfo resolveInfo: resolvedInfoList){
            if(resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")){
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name );
                resolved = true;
                break;
            }
        }
        if(resolved){
            activity.startActivity(tweetIntent);
        }else{
            Toast.makeText(activity.getApplication(), "Twitter app isn't found", Toast.LENGTH_LONG).show();
        }
    }


    public static void postTumblr(Activity activity, String bodyText, Uri editedImageUri){

        Intent tumblrIntent = new Intent(Intent.ACTION_SEND);
        tumblrIntent.putExtra(Intent.EXTRA_TEXT, bodyText);
        tumblrIntent.putExtra(Intent.EXTRA_STREAM, editedImageUri);
        tumblrIntent.setType("image/png");
        tumblrIntent.setPackage("com.tumblr");

        PackageManager packManager = activity.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tumblrIntent,  PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for(ResolveInfo resolveInfo: resolvedInfoList){
            if(resolveInfo.activityInfo.packageName.startsWith("com.tumblr")){
                tumblrIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name );
                resolved = true;
                break;
            }
        }
        if(resolved){
            activity.startActivity(tumblrIntent);
        }else{
            Toast.makeText(activity.getApplication(), "Tumblr app isn't found", Toast.LENGTH_LONG).show();
        }
    }


    public static void postInstagram(Activity activity, String bodyText, Uri editedImageUri){

        Intent instagramIntent = new Intent(Intent.ACTION_SEND);
        //instagramIntent.putExtra(Intent.EXTRA_TEXT, bodyText);
        instagramIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + editedImageUri));
        instagramIntent.setType("image/*");
        instagramIntent.setPackage("com.instagram.android");

        Log.d(TAG, "IMAGE URL: " + editedImageUri);
        PackageManager packManager = activity.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(instagramIntent,  PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for(ResolveInfo resolveInfo: resolvedInfoList){
            if(resolveInfo.activityInfo.packageName.startsWith("com.instagram.android")){
                instagramIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name );
                resolved = true;
                break;
            }
        }
        if(resolved){
            activity.startActivity(instagramIntent);
        }else{
            Toast.makeText(activity.getApplication(), "Instagram app isn't found", Toast.LENGTH_LONG).show();
        }
    }


    public static void postSnapChat(Activity activity, String bodyText, Uri editedImageUri){

        ///------------ Images too large, need to compress ------////

        Intent snapChatIntent = new Intent(Intent.ACTION_SEND);
        snapChatIntent.putExtra(Intent.EXTRA_TEXT, bodyText);
        snapChatIntent.putExtra(Intent.EXTRA_STREAM, snapChatIntent);
        snapChatIntent.setType("image/*");
        snapChatIntent.setPackage("com.snapchat.android");

        Log.d(TAG, "IMAGE URL: " + editedImageUri);
        PackageManager packManager = activity.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(snapChatIntent,  PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for(ResolveInfo resolveInfo: resolvedInfoList){
            if(resolveInfo.activityInfo.packageName.startsWith("com.snapchat.android")){
                snapChatIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name );
                resolved = true;
                break;
            }
        }
        if(resolved){
            activity.startActivity(snapChatIntent);
        }else{
            Toast.makeText(activity.getApplication(), "SnapChat app isn't found", Toast.LENGTH_LONG).show();
        }
    }



    public static void postPinterest(Activity activity, String bodyText, Uri editedImageUri){

        String shareUrl = "";//http://stackoverflow.com/questions/27388056/";
        String mediaUrl = "http://cdn.sstatic.net/stackexchange/img/logos/so/so-logo.png";
        String description = "test";
        String url = String.format(
                "https://www.pinterest.com/pin/create/button/?url=%s&media=%s&description=%s",
                urlEncode(shareUrl), urlEncode(mediaUrl), description);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        filterByPackageName(activity, intent, "com.pinterest");
        activity.startActivity(intent);

//        Intent pinterestIntent = new Intent(Intent.ACTION_SEND);
//        //pinterestIntent.putExtra(Intent.EXTRA_TEXT, bodyText);
//        pinterestIntent.putExtra("com.pinterest.EXTRA_DESCRIPTION",bodyText);
//        pinterestIntent.putExtra(Intent.EXTRA_STREAM, pinterestIntent);
//        pinterestIntent.setType("image/*");
//        pinterestIntent.setPackage("com.pinterst");
//
//        Log.d(TAG, "IMAGE URL: " + editedImageUri);
//        PackageManager packManager = activity.getPackageManager();
//        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(pinterestIntent,  PackageManager.MATCH_DEFAULT_ONLY);
//
//        boolean resolved = false;
//        for(ResolveInfo resolveInfo: resolvedInfoList){
//            if(resolveInfo.activityInfo.packageName.startsWith("com.pinterest")){
//                pinterestIntent.setClassName(
//                        resolveInfo.activityInfo.packageName,
//                        resolveInfo.activityInfo.name );
//                resolved = true;
//                break;
//            }
//        }
//        if(resolved){
//            activity.startActivity(pinterestIntent);
//        }else{
//            Toast.makeText(activity.getApplication(), "Pinterst app isn't found", Toast.LENGTH_LONG).show();
//        }
    }



    public static void filterByPackageName(Context context, Intent intent, String prefix) {
        List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith(prefix)) {
                intent.setPackage(info.activityInfo.packageName);
                return;
            }
        }
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.wtf("", "UTF-8 should always be supported", e);
            return "";
        }
    }


    public static Uri takePhoto(Activity activity){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        Uri imageUri = Uri.fromFile(photo);
        activity.startActivityForResult(intent, Constants.TAKE_PICTURE);
        return imageUri;
    }


    public static int convertColorHexToResource(String hexColor){
        return Color.parseColor(hexColor);
    }

    public static void setFabButton(Context context, FloatingActionButton fab, int icon) {
        int color = ContextCompat.getColor(context, FAB_COLOR);

        fab.setImageResource(icon);
        fab.setColorFilter(color);
    }



//// ------- Not Being used

    private void initSeekBar(SeekBar seekBar, final ImageView imageView, final Activity activity) {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                imageView.setImageBitmap(changeBitmapContrastBrightness(BitmapFactory.decodeResource(activity.getResources(), R.drawable.android_arms), (float) progress / 100f, 1));
                //textView.setText("Contrast: "+(float) progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBar.setMax(200);
        seekBar.setProgress(100);
        seekBar.incrementProgressBy(1);
    }

    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }
}
