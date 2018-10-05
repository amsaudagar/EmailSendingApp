package mvp.android.filechooser.presenter;

import android.content.Context;

import mvp.android.filechooser.view.IMainView;

//TODO Implement this class to have all the business logic from activity class to here
public class MainPresenter implements IMainPresenter{

    private Context mContext;
    private IMainView mView;

    public MainPresenter(Context context, IMainView view) {
        mContext = context;
        mView = view;
    }
}
