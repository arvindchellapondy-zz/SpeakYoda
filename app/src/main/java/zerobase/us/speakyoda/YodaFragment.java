package zerobase.us.speakyoda;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tt.whorlviewlibrary.WhorlView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by arvindchellapondy on 12/2/15.
 */
public class YodaFragment extends Fragment {

    private View view;

    @Bind(R.id.whorl_view)
    WhorlView whorlView;

    @Bind(R.id.human_edit_text_view)
    EditText humanText;

    @Bind(R.id.convert_button)
    Button convertButton;

    @Bind(R.id.clear_button)
    Button clearButton;

    @Bind(R.id.yoda_text_layout)
    RelativeLayout yodaTextLayout;

    @Bind(R.id.yoda_text_view)
    TextView yodaTextView;

    @Bind(R.id.loader_layout)
    RelativeLayout loaderLayout;

    @Bind(R.id.share_button)
    Button shareButton;

    private final static int MY_SOCKET_TIMEOUT_MS = 30000;
    private final static String HIDE = "HIDE";
    private final static String SHOW = "SHOW";
    private final static String YODA_TEXT_VIEW = "YODA_TEXT_VIEW";
    private final static String YODA_LOADER_VIEW = "YODA_LOADER_VIEW";
    private final static String BASE_URL = "https://yoda.p.mashape.com/yoda";
    private final static String ENDPOINT = "?sentence=";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_yoda, container, false);
        ButterKnife.bind(this, view);
        setupView();
        return view;
    }

    private void setupView() {

        yodaTextView.setMovementMethod(new ScrollingMovementMethod());

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(humanText.getText().toString())) {
                    keyboardAction(HIDE);
                    toggleYodaView(YODA_LOADER_VIEW, null);
                    String url = "";
                    try {
                        url = constructUrl();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
                    StringRequest myReq = new StringRequest(Request.Method.GET,
                            url,
                            createMyReqSuccessListener(),
                            createMyReqErrorListener()) {

                        @Override
                        public String getBodyContentType() {
                            return "text/html";
                        }

                        @Override
                        public HashMap<String, String> getHeaders() {
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put("X-Mashape-Key", "FdqDuXOgjvmshlefjJTzJKvv0rqTp1Li7jWjsnXVANb63dkBqE");
                            return params;
                        }

                    };

                    myReq.setRetryPolicy(new DefaultRetryPolicy(
                            MY_SOCKET_TIMEOUT_MS,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    queue.add(myReq);

                } else {
                    keyboardAction(HIDE);
                    Snackbar snackbar = Snackbar
                            .make(loaderLayout, R.string.type_something_text, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                humanText.setText("");
                keyboardAction(SHOW);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String shareBody = yodaTextView.getText().toString();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, getActivity().getString(R.string.share_text)));

            }
        });

    }

    private Response.Listener<String> createMyReqSuccessListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String yodaText = response.toString();
                if (!TextUtils.isEmpty(yodaText)) {
                    toggleYodaView(YODA_TEXT_VIEW, yodaText);
                } else {
                    toggleYodaView(YODA_TEXT_VIEW, getActivity().getString(R.string.no_response));
                }

            }
        };
    }

    private Response.ErrorListener createMyReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorText = getActivity().getString(R.string.please_try_again);
                if (!TextUtils.isEmpty(errorText)) {
                    toggleYodaView(YODA_TEXT_VIEW, errorText);
                } else {
                    toggleYodaView(YODA_TEXT_VIEW, getActivity().getString(R.string.no_error));
                }

            }
        };
    }

    private void toggleYodaView(String view, String yodaText) {

        if (view == YODA_LOADER_VIEW) {
            whorlView.start();
            yodaTextLayout.setVisibility(View.INVISIBLE);
            loaderLayout.setVisibility(View.VISIBLE);
        } else {
            whorlView.stop();
            if (!TextUtils.isEmpty(yodaText)) {
                yodaTextView.setText(yodaText);
            }
            yodaTextLayout.setVisibility(View.VISIBLE);
            loaderLayout.setVisibility(View.INVISIBLE);
        }

    }

    private String constructUrl() throws UnsupportedEncodingException {
        String url = "";
        String sentence = constructSentence();
        url = BASE_URL + ENDPOINT + sentence;
        Log.e("Url : ", url);
        return url;
    }

    private String constructSentence() throws UnsupportedEncodingException {
        String sentence = "";
        sentence = URLEncoder.encode(humanText.getText().toString(), "UTF-8");
        return sentence;
    }

    private void keyboardAction(String action) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            if (action == HIDE) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } else {
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

}
