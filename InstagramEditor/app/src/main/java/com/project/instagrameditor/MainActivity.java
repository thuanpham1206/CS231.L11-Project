package com.project.instagrameditor;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.project.instagrameditor.utils.BitmapUtils;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ColorOverlaySubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.VignetteSubfilter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements FiltersListFragment.FiltersListFragmentListener, EditImageFragment.EditImageFragmentListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int SELECT_GALLERY_IMAGE = 101;

    private static final int CAMERA_REQUEST = 100;

    private Uri saveImageUri;
    private Uri imageUri;
    private String imagePath;
    private String imageurl;

    @BindView(R.id.image_preview)
    ImageView imagePreview;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    Bitmap originalImage;
    // to backup image with filter applied
    Bitmap filteredImage;

    // the final image after applying filters
    Bitmap finalImage;

    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;

    // modified image values
    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float contrastFinal = 1.0f;
    float redFinal = 0.0f;
    float blueFinal = 0.0f;
    float greenFinal = 0.0f;
    int vignetteFinal = 0;
    float sharpenFinal = 0.0f;
    int ksizeFinal = 0;

    // for sharpening and smoothing
    Mat before, after;

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.activity_title_main));

        loadImage();

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        // render selected image thumbnails
        filtersListFragment.prepareThumbnail(null);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // adding filter list fragment
        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);

        // adding edit image fragment
        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);

        adapter.addFragment(filtersListFragment, getString(R.string.tab_filters));
        adapter.addFragment(editImageFragment, getString(R.string.tab_edit));

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFilterSelected(Filter filter) {
        // reset image controls
        resetControls();

        // applying the selected filter
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        // preview filtered image
        imagePreview.setImageBitmap(filter.processFilter(filteredImage));

        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    @Override
    public void onBrightnessChanged(final int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onSaturationChanged(final float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onContrastChanged(final float contrast) {
        contrastFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onColorOverlayChanged(final float red, final float blue, final float green) {
        redFinal = red;
        blueFinal = blue;
        greenFinal = green;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ColorOverlaySubFilter(100, red, blue, green));
        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onVignetteChanged(final int vignette) {
        vignetteFinal = vignette;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new VignetteSubfilter(this, vignette));
        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onSharpenChanged(final float sharpen){
        sharpenFinal = sharpen;
    }

    @Override
    public void onSmoothChanged(final int ksize){
        if (ksize%2 == 1){
            ksizeFinal = ksize;
        }
    }

    public Bitmap SharpenSubFilter(Bitmap bitmap, float sharpen){
        if(sharpen <= 0.10f){
            return bitmap;
        }
        Utils.bitmapToMat(bitmap, before);
        Mat after = new Mat(before.rows(), before.cols(), CvType.CV_8UC1);
        Mat kernel = new Mat(3, 3, CvType.CV_32F){
            {
                put(0, 0, 0.0f, -1.0f, 0.0f);
                put(1, 0, -1.0f, 4.8f + sharpen, -1.0f);
                put(2, 0, 0.0f, -1.0f, 0.0f);
            }
        };
        Imgproc.filter2D(before, after, -1, kernel);
        Bitmap bmp = Bitmap.createBitmap(after.cols(), after.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(after, bmp);
        return bmp;
    }

    public Bitmap SmoothSubFilter(Bitmap bitmap, int ksize){
        if (ksize > 2){
            Utils.bitmapToMat(bitmap, before);
            Mat after = new Mat(before.rows(), before.cols(), CvType.CV_8UC1);
            Imgproc.GaussianBlur(before, after, new Size(ksize,ksize), 0);
            Bitmap bmp = Bitmap.createBitmap(after.cols(), after.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(after, bmp);
            return bmp;
        }
        else {
            return bitmap;
        }
    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditCompleted() {
        // once the editing is done i.e seekbar is drag is completed,
        // apply the values on to filtered image
        final Bitmap bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true);

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new ContrastSubFilter(contrastFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        myFilter.addSubFilter(new VignetteSubfilter(this, vignetteFinal));
        //myFilter.addSubFilter(new ColorOverlaySubFilter(100, redFinal, greenFinal, blueFinal));
        finalImage = myFilter.processFilter(bitmap);
        finalImage = SharpenSubFilter(finalImage, sharpenFinal);
        finalImage = SmoothSubFilter(finalImage, ksizeFinal);
        imagePreview.setImageBitmap(finalImage);
    }

    /**
     * Resets image edit controls to normal when new filter
     * is selected
     */
    private void resetControls() {
        if (editImageFragment != null) {
            editImageFragment.resetControls();
        }
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        contrastFinal = 1.0f;
        vignetteFinal = 0;
        redFinal = 0.0f;
        blueFinal = 0.0f;
        greenFinal = 0.0f;
        sharpenFinal = 0.0f;
        ksizeFinal = 0;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    // load the default image from assets on app launch
    private void loadImage() {
        originalImage = BitmapUtils.decodeSampledBitmapFromResource(this.getResources(), R.drawable.sana,300, 300);
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        imagePreview.setImageBitmap(originalImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open) {
            openImageFromGallery();
            return true;
        }

        if (id == R.id.action_save) {
            saveImageToGallery();
            return true;
        }

        if (id == R.id.action_camera) {
            openCamera();
            return true;
        }

        if (id == R.id.action_share) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case SELECT_GALLERY_IMAGE:
                    Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);

                    // clear bitmap memory
                    originalImage.recycle();
                    filteredImage.recycle();
                    finalImage.recycle();

                    originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
                    finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
                    imagePreview.setImageBitmap(originalImage);
                    bitmap.recycle();
                    break;

                case CAMERA_REQUEST:
                    try {
                        // clear bitmap memory
                        originalImage.recycle();
                        filteredImage.recycle();
                        finalImage.recycle();

                        //Bundle extras = data.getExtras();
                        //Bitmap photo = (Bitmap) extras.get("data");

                        Bitmap bmp = BitmapUtils.getBitmapFromGallery(this, imageUri, 800, 800);
                        Bitmap photo = rotateImage(bmp, 90);

                        originalImage = photo.copy(Bitmap.Config.ARGB_8888, true);
                        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
                        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
                        imagePreview.setImageBitmap(photo);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void openImageFromGallery() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_GALLERY_IMAGE);
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /*
     * saves image to camera gallery
     * */
    private void saveImageToGallery() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final String path = BitmapUtils.insertImage(getContentResolver(), finalImage, System.currentTimeMillis() + "_InstaEdit.jpg", null);
                            saveImageUri = Uri.fromFile(new File(path));
                            if (!TextUtils.isEmpty(path)) {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Image saved to gallery!", Snackbar.LENGTH_LONG)
                                        .setAction("OPEN", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openImage(path);
                                            }
                                        });

                                snackbar.show();
                            } else {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Unable to save image!", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void openCamera() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, "New Picture");
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(intent, CAMERA_REQUEST);
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    // opening image in default image viewer app
    private void openImage(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "image/*");
        startActivity(intent);
    }

    private void shareImage() {
        if (saveImageUri == null) {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.msg_save_image_to_share), Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, saveImageUri);
        startActivity(Intent.createChooser(intent, getString(R.string.msg_share_image)));
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    before = new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

}