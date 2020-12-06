package com.project.instagrameditor;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditImageFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private EditImageFragmentListener listener;

    @BindView(R.id.seekbar_brightness)
    SeekBar seekBarBrightness;

    @BindView(R.id.seekbar_contrast)
    SeekBar seekBarContrast;

    @BindView(R.id.seekbar_saturation)
    SeekBar seekBarSaturation;

    /*
    @BindView(R.id.seekbar_red)
    SeekBar seekBarRed;

    @BindView(R.id.seekbar_blue)
    SeekBar seekBarBlue;

    @BindView(R.id.seekbar_green)
    SeekBar seekBarGreen;
    */

    @BindView(R.id.seekbar_vignette)
    SeekBar seekBarVignette;

    public void setListener(EditImageFragmentListener listener) {
        this.listener = listener;
    }

    public EditImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_image, container, false);

        ButterKnife.bind(this, view);

        // keeping brightness value b/w -100 / +100
        seekBarBrightness.setMax(200);
        seekBarBrightness.setProgress(100);

        // keeping contrast value b/w 1.0 - 3.0
        seekBarContrast.setMax(20);
        seekBarContrast.setProgress(0);

        // keeping saturation value b/w 0.0 - 3.0
        seekBarSaturation.setMax(30);
        seekBarSaturation.setProgress(10);

        // keeping brightness value b/w 0 - 255
        seekBarVignette.setMax(255);
        seekBarVignette.setProgress(0);

        /*
        // keeping red value b/w 0.0 - 1.0
        seekBarRed.setMax(10);
        seekBarRed.setProgress(0);

        // keeping blue value b/w 0.0 - 1.0
        seekBarBlue.setMax(10);
        seekBarBlue.setProgress(0);

        // keeping green value b/w 0.0 - 1.0
        seekBarGreen.setMax(10);
        seekBarGreen.setProgress(0);
         */

        seekBarBrightness.setOnSeekBarChangeListener(this);
        seekBarContrast.setOnSeekBarChangeListener(this);
        seekBarSaturation.setOnSeekBarChangeListener(this);
        seekBarVignette.setOnSeekBarChangeListener(this);
        /*
        seekBarRed.setOnSeekBarChangeListener(this);
        seekBarBlue.setOnSeekBarChangeListener(this);
        seekBarGreen.setOnSeekBarChangeListener(this);
         */

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (listener != null) {

            if (seekBar.getId() == R.id.seekbar_brightness) {
                // brightness values are b/w -100 to +100
                listener.onBrightnessChanged(progress - 100);
            }

            if (seekBar.getId() == R.id.seekbar_contrast) {
                // converting int value to float
                // contrast values are b/w 1.0f - 3.0f
                // progress = progress > 10 ? progress : 10;
                progress += 10;
                float floatVal = .10f * progress;
                listener.onContrastChanged(floatVal);
            }

            if (seekBar.getId() == R.id.seekbar_saturation) {
                // converting int value to float
                // saturation values are b/w 0.0f - 3.0f
                float floatVal = .10f * progress;
                listener.onSaturationChanged(floatVal);
            }

            if (seekBar.getId() == R.id.seekbar_vignette) {
                int alpha = progress;
                listener.onVignetteChanged(alpha);
            }

            /*
            float floatVal = .10f;
            float floatRed, floatBlue, floatGreen;
            if (seekBar.getId() == R.id.seekbar_red) {
                // converting int value to float
                // red values are b/w 0.0f - 1.0f
                floatRed = floatVal * progress;
                floatBlue = seekBarBlue.getProgress();
                floatGreen = seekBarGreen.getProgress();
                listener.onColorOverlayChanged(floatRed, floatBlue, floatGreen);
            }
            if (seekBar.getId() == R.id.seekbar_blue) {
                // converting int value to float
                // blue values are b/w 0.0f - 1.0f
                floatRed = seekBarRed.getProgress();
                floatBlue = floatVal * progress;
                floatGreen = seekBarGreen.getProgress();
                listener.onColorOverlayChanged(floatRed, floatBlue, floatGreen);
            }
            if (seekBar.getId() == R.id.seekbar_green) {
                // converting int value to float
                // green values are b/w 0.0f - 1.0f
                floatRed = seekBarRed.getProgress();
                floatBlue = seekBarBlue.getProgress();
                floatGreen = floatVal * progress;
                listener.onColorOverlayChanged(floatRed, floatBlue, floatGreen);
            }

             */
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onEditStarted();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onEditCompleted();
    }

    public void resetControls() {
        seekBarBrightness.setProgress(100);
        seekBarContrast.setProgress(0);
        seekBarSaturation.setProgress(10);
        seekBarVignette.setProgress(0);
    }

    public interface EditImageFragmentListener {
        void onBrightnessChanged(int brightness);

        void onSaturationChanged(float saturation);

        void onContrastChanged(float contrast);

        void onColorOverlayChanged(float red, float blue, float green);

        void onVignetteChanged(int alpha);

        void onEditStarted();

        void onEditCompleted();
    }
}