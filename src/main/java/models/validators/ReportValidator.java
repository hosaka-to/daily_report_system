package models.validators;

import java.util.ArrayList;
import java.util.List;

import actions.views.ReportView;
import constants.MessageConst;

/**
 * 日報インスタンスに設定されている値のバリデーションを行うクラス
 */
public class ReportValidator {

    /**
     * 日報インスタンスの各項目についてバリデーションを行う
     * @param rv 日報インスタンス
     * @return エラーのリスト
     */
    public static List<String> validate(ReportView rv){
        List<String> errors = new ArrayList<String>();

        //タイトルのチェック
        String titleError = validateTitle(rv.getTitle());
        if(!titleError.equals("")) {
            errors.add(titleError);
        }

        //内容のチェック
        String contentError=validateContent(rv.getContent());
        if(!contentError.equals("")) {
            errors.add(contentError);
        }

        //内容のチェック
        String startTimeError = validateStartTime(rv.getStartTime());
        if (!startTimeError.equals("")) {
            errors.add(startTimeError);
        }

        //内容のチェック
        String finishTimeError = validateFinishTime(rv.getFinishTime());
        if (!finishTimeError.equals("")) {
            errors.add(finishTimeError);
        }

        return errors;
    }



    /**
     * タイトルに入力値があるかをチェックし、入力値がなければエラーメッセージを返却
     * @param title タイトル
     * @return エラーメッセージ
     */
    private static String validateTitle(String title) {
        if(title == null || title.equals("")) {
            return MessageConst.E_NOTITLE.getMessage();
        }

        //入力値がある場合は空文字を返却
        return "";
    }

    /**
     * 内容に入力値があるかをチェックし、入力値がなければエラーメッセージを返却
     * @param content 内容
     * @return エラーメッセージ
     */
    private static String validateContent(String content) {
        if(content == null || content.equals("")) {
            return MessageConst.E_NOCONTENT.getMessage();
        }

        //入力値がある場合は空文字を返却
        return "";
    }

    /**
     * 内容に入力値があるかをチェックし、入力値がなければエラーメッセージを返却
     * @param startTime 内容
     * @return エラーメッセージ
     */
    private static String validateStartTime(String startTime) {
        if (startTime == null || startTime.equals("")) {
            return MessageConst.E_NOSTARTTIME.getMessage();
        }

        //入力値がある場合は空文字を返却
        return "";
    }

    /**
     * 内容に入力値があるかをチェックし、入力値がなければエラーメッセージを返却
     * @param finishTime 内容
     * @return エラーメッセージ
     */
    private static String validateFinishTime(String finishTime) {
        if (finishTime == null || finishTime.equals("")) {
            return MessageConst.E_NOFINISHTIME.getMessage();
        }

        //入力値がある場合は空文字を返却
        return "";
    }
}
