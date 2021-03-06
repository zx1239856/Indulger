package com.inftyloop.indulger.ui;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebSettings;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.inftyloop.indulger.R;
import butterknife.BindView;
import com.inftyloop.indulger.model.entity.NewsEntry;
import com.inftyloop.indulger.model.entity.NewsFavEntry;
import com.inftyloop.indulger.util.*;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;

public class NewsDetailHeaderView extends FrameLayout {
    private static final String NICK = "Indulger";  // used to bind javascript
    @BindView(R.id.tvTitle)
    TextView mTitle;
    @BindView(R.id.ll_info)
    LinearLayout mllInfo;
    @BindView(R.id.iv_avatar)
    ImageView mAvatar;
    @BindView(R.id.tv_author)
    TextView mAuthor;
    @BindView(R.id.tv_time)
    TextView mTime;
    @BindView(R.id.wv_content)
    NestedWebView mContent;
    @BindView(R.id.btn_fav)
    Button mFavBtn;

    private boolean mHasFollowed;
    private Context mContext;
    private LoadWebListener mWebListener;

    public NewsDetailHeaderView(Context ctx) {
        this(ctx, null);
    }

    public NewsDetailHeaderView(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, 0);
    }

    public NewsDetailHeaderView(Context ctx, AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        mContext = ctx;
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.header_news_detail, this);
        ButterKnife.bind(this, this);
        mHasFollowed = false;
        mContent.getSettings().setAppCacheEnabled(true);
        mContent.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mContent.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        mFavBtn.setVisibility(View.GONE);
    }

    private void addJs(WebView wv) {
        wv.loadUrl("javascript:(function pic(){ var imgs = document.getElementsByTagName(\"img\");" +
                "for(var i = 0; i < imgs.length; ++i) { var img = imgs[i];" +
                "img.onclick=function(){ Indulger.openImg(this.src);} }" +
                " })()");
    }

    public void setNewsDetail(NewsEntry detail, LoadWebListener listener) {
        mWebListener = listener;
        mTitle.setText(detail.getTitle());
        if(detail.getPublisherName().isEmpty())
            mllInfo.setVisibility(GONE);
        else {
            if(!TextUtils.isEmpty(detail.getPublisherAvatarUrl()))
                GlideImageLoader.loadRound(mContext, detail.getPublisherAvatarUrl(), mAvatar, R.mipmap.ic_circle_default);
            else
                mAvatar.setVisibility(GONE);
            mAuthor.setText(detail.getPublisherName());
            mTime.setText(DateUtils.getShortTime(mContext, detail.getPublishTime()));
        }
        if(TextUtils.isEmpty(detail.getContent()))
            mContent.setVisibility(GONE);
        mContent.getSettings().setJavaScriptEnabled(true);
        mContent.addJavascriptInterface(new ShowPicJSBridge(mContext, detail.getImageUrls()), NICK);
        // change background according to theme
        String bg_color = DisplayHelper.getColorStringFromAttr(mContext, R.attr.app_background_color);
        String text_color = DisplayHelper.getColorStringFromAttr(mContext, R.attr.foreground_text_color);

        String htmlPart1 = "<!DOCTYPE HTML html>\n" +
                "<head><meta charset=\"utf-8\"/>\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, user-scalable=no\"/>\n" +
                "</head>\n" +
                "<body>\n" +
                "<style> \n" +
                "img{width:100%!important;height:auto!important}\n" +
                "body{ background-color: " + bg_color + ";\n" + "color: " + text_color +  ";\n}" +
                " </style>";
        String htmlPart2 = "</body></html>";

        String html = htmlPart1 + detail.getContent() + htmlPart2;

        mContent.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        mContent.setWebViewClient(new ControlledWebViewClient(ControlledWebViewClient.DISABLE_ANY_LINK) {
            @Override
            public void onPageFinished(WebView view, String url) {
                addJs(view);
                if (mWebListener != null) {
                    mWebListener.onLoaded();
                }
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if(url.startsWith("http")) {
                    try {
                        final Call call = OkHTTPImageClient.getInstance().newCall(
                                new Request.Builder().url(url).build()
                        );
                        final Response response = call.execute();
                        return new WebResourceResponse(response.header("content-type"),
                                response.header("content-encoding", "utf-8"),
                                response.body().byteStream());
                    } catch (Exception e) {
                        return super.shouldInterceptRequest(view, url);
                    }
                }
                else
                    return super.shouldInterceptRequest(view, url);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                if(url.getScheme().equals("http") || url.getScheme().equals("https")) {
                    try {
                        final Call call = OkHTTPImageClient.getInstance().newCall(
                                new Request.Builder().url(url.toString()).build()
                        );
                        final Response response = call.execute();
                        return new WebResourceResponse(response.header("content-type"),
                                response.header("content-encoding", "utf-8"),
                                response.body().byteStream());
                    } catch (Exception e) {
                        return super.shouldInterceptRequest(view, request);
                    }
                }
                else
                    return super.shouldInterceptRequest(view, request);
            }
        });
    }

    @OnClick(R.id.btn_fav)
    public void onFollowBtnClick(View view) {
        mFavBtn.setText(getContext().getString(mHasFollowed ? R.string.follow : R.string.followed));
        mHasFollowed = !mHasFollowed;
    }

    public interface LoadWebListener {
        void onLoaded();
    }
}
