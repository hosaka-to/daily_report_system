package actions;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import actions.views.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import services.ReportService;

/**
 * 日報に関する処理を行うActionクラス
 */
public class ReportAction extends ActionBase{

    private ReportService service;

    /**
     * メソッドの実行
     */
    @Override
    public void process() throws ServletException,IOException{

        service = new ReportService();

        //メソッドの実行
        invoke();
        service.close();
    }
    /**
     * 一覧画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException,IOException{

        //指定されたページ数の一覧画面に表示する日報データを取得
        int page=getPage();
        List<ReportView>reports = service.getAllPerPage(page);

        //全日報データの件数を取得
        long reportsCount = service.countAll();

        putRequestScope(AttributeConst.REPORTS,reports);
        putRequestScope(AttributeConst.REP_COUNT,reportsCount);
        putRequestScope(AttributeConst.PAGE,page);
        putRequestScope(AttributeConst.MAX_ROW,JpaConst.ROW_PER_PAGE);

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if(flush!=null) {
            putRequestScope(AttributeConst.FLUSH,flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //一覧画面を表示
        forward(ForwardConst.FW_REP_INDEX);
    }

    /**
     * 新規登録画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void entryNew() throws ServletException,IOException{

        putRequestScope(AttributeConst.TOKEN,getTokenId());

        ReportView rv=new ReportView();
        rv.setReportDate(LocalDate.now());
        putRequestScope(AttributeConst.REPORT,rv);

        //新規登録画面を表示
        forward(ForwardConst.FW_REP_NEW);
    }

    /**
     * 新規登録を行う
     * @throws ServletException
     * @throws IOException
     */
    public void create() throws ServletException,IOException{

        //CSRF対策
        if(checkToken()) {

            //日報の日付が入力されていなければ、今日の日付を設定
            LocalDate day=null;
            if(getRequestParam(AttributeConst.REP_DATE) == null
                    || getRequestParam(AttributeConst.REP_DATE).equals("")) {
                day = LocalDate.now();
            } else {
                day = LocalDate.parse(getRequestParam(AttributeConst.REP_DATE));
            }

            //セッションからログイン中の従業員情報を取得
            EmployeeView ev=(EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

            LocalDateTime startDateTime = LocalDateTime.parse(getRequestParam(AttributeConst.REP_STARTTIME),dateTimeFormatter);
            LocalDateTime finishDateTime = LocalDateTime.parse(getRequestParam(AttributeConst.REP_FINISHTIME),dateTimeFormatter);

            //パラメータの値をもとに日報情報のインスタンスを作成する
            ReportView rv =new ReportView(
                    null,
                    ev,
                    day,
                    getRequestParam(AttributeConst.REP_TITLE),
                    getRequestParam(AttributeConst.REP_CONTENT),
                    null,
                    null,
                    startDateTime,
                    finishDateTime);

            //日報情報登録
            List<String>errors=service.create(rv);

            if(errors.size() > 0){
                //登録中にエラーがあった場合

                putRequestScope(AttributeConst.TOKEN,getTokenId());
                putRequestScope(AttributeConst.REPORT,rv);
                putRequestScope(AttributeConst.ERR,errors);

                //新規登録画面を再表示
                forward(ForwardConst.FW_REP_NEW);

            }else {
                //登録中にエラーがなかった場合

                //セッションに登録完了のフラッシュメッセージ設定
                putSessionScope(AttributeConst.FLUSH,MessageConst.I_REGISTERED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP,ForwardConst.CMD_INDEX);
            }
    }
}

    /**
     * 詳細画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void show() throws ServletException,IOException{

        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        if(rv == null) {
            //該当の日報データが存在しない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);

        }else {

            putRequestScope(AttributeConst.REPORT,rv);

            //詳細画面を表示
            forward(ForwardConst.FW_REP_SHOW);
        }
    }
    /**
     * 編集画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void edit() throws ServletException,IOException{

        //idを条件に日報データを取得
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView)getSessionScope(AttributeConst.LOGIN_EMP);

        if(rv == null || ev.getId() != rv.getEmployee().getId()) {
            //該当の日報データが存在しないまたは
            //ログインしている従業員が日報の作成者でない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);

        }else {

            putRequestScope(AttributeConst.TOKEN,getTokenId());
            putRequestScope(AttributeConst.REPORT,rv);

            //編集画面を表示
            forward(ForwardConst.FW_REP_EDIT);
        }
    }
    /**
     * 更新を行う
     * @throws ServletException
     * @throws IOException
     */
    public void update()throws ServletException,IOException{

        //CSRF対策
        if(checkToken()) {

            //idを条件に日報データを取得
            ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

            //入力された日報内容を設定する
            rv.setReportDate(toLocalDate(getRequestParam(AttributeConst.REP_DATE)));
            rv.setTitle(getRequestParam(AttributeConst.REP_TITLE));
            rv.setContent(getRequestParam(AttributeConst.REP_CONTENT));
            rv.setStartTime(toLocalDateTime(getRequestParam(AttributeConst.REP_STARTTIME)));
            rv.setFinishTime(toLocalDateTime(getRequestParam(AttributeConst.REP_FINISHTIME)));

            //日報データを更新
            List<String> errors = service.update(rv);

            if(errors.size() > 0) {
                //更新中にエラーが発生した場合

                putRequestScope(AttributeConst.TOKEN,getTokenId());
                putRequestScope(AttributeConst.REPORT,rv);
                putRequestScope(AttributeConst.ERR,errors);

                //編集画面を再表示
                forward(ForwardConst.FW_REP_EDIT);
            }else {
                //更新中にエラーがなかった場合
                putSessionScope(AttributeConst.FLUSH,MessageConst.I_UPDATED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP,ForwardConst.CMD_INDEX);
            }
        }
    }
}
