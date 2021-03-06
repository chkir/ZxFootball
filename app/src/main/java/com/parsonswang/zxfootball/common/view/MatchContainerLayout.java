package com.parsonswang.zxfootball.common.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.parsonswang.common.utils.StringUtils;
import com.parsonswang.common.utils.UIUtils;
import com.parsonswang.zxfootball.bean.MatchTimelines;
import com.parsonswang.zxfootball.bean.PlayerInfo;
import com.parsonswang.zxfootball.common.Constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

/**
 * 比赛球员信息container
 */
public class MatchContainerLayout extends TableLayout {

    public MatchContainerLayout(@NonNull Context context) {
        super(context);
    }

    public MatchContainerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setGravity(VERTICAL);

    }

    public void addPlayer(String homeTeamFormation,
                          LinkedList<PlayerInfo> homeMainPlayerInfos,
                          LinkedList<PlayerInfo> homeBenchPlayerInfos,
                          String awayTeamFormation,
                          LinkedList<PlayerInfo> awayMainPlayerInfos,
                          LinkedList<PlayerInfo> awayBenchPlayerInfos,
                          int height,
                          HashMap<String, ArrayList<MatchTimelines>> matchTimelines,
                          final SparseIntArray timeLineEventResMap) {
        //=====主队球员（首发）=====
        final ArrayList<String> homeRowInfo = getRowCnt(homeTeamFormation);
        int homeRowCnt = homeRowInfo.size() + 1;

        final int homeRowHeight = height / 2 / homeRowCnt;
        Timber.i("matchTimelines: " + matchTimelines);

        for (int i = 0; i < homeRowCnt; i++) {
            TableRow tableRow = new TableRow(getContext());
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = homeRowHeight;
            tableRow.setOrientation(HORIZONTAL);
            addView(tableRow, layoutParams);

            if (i == 0) {//主队门将位置
                MatchPlayerView playerView = new MatchPlayerView(getContext());
                final PlayerInfo playerInfo = homeMainPlayerInfos.poll();
                if (playerInfo != null) {
                    final ArrayList<MatchTimelines> matchTimelinesArrayList = matchTimelines.get(playerInfo.playerId);
                    playerView.setData(playerInfo, matchTimelinesArrayList, timeLineEventResMap);
                }

                tableRow.addView(playerView);
                tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
                playerView.getLayoutParams().height = homeRowHeight;
//                ((LinearLayout.LayoutParams)playerView.getLayoutParams()).topMargin = UIUtils.dip2px(getContext(), 5);
            } else {
//                ((LinearLayout.LayoutParams)tableRow.getLayoutParams()).topMargin = UIUtils.dip2px(getContext(), 15);

                int homeCntPerRow =  Integer.parseInt(homeRowInfo.get(i - 1));
                for (int j = 0; j < homeCntPerRow; j++) {
                    final MatchPlayerView playerView = new MatchPlayerView(getContext());
                    tableRow.addView(playerView);
                    final PlayerInfo homeMainPlayerInfo = homeMainPlayerInfos.poll();
                    if (homeMainPlayerInfo != null) {
                        final ArrayList<MatchTimelines> matchTimelinesArrayList = matchTimelines.get(homeMainPlayerInfo.playerId);
                        playerView.setData(homeMainPlayerInfo, matchTimelinesArrayList, timeLineEventResMap);
                        if (homeMainPlayerInfo.hasExchangeDown) {
                            final PlayerInfo homeBenchPlayerInfo = homeBenchPlayerInfos.poll();
                            final ArrayList<MatchTimelines> matchBenchTimelinesArrayList = matchTimelines.get(homeBenchPlayerInfo.playerId);
                            playerView.substitution(homeBenchPlayerInfo, matchBenchTimelinesArrayList, timeLineEventResMap);
                        }
                    }

                    tableRow.setWeightSum(homeCntPerRow);
                    tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
                    ((LinearLayout.LayoutParams)playerView.getLayoutParams()).weight = 1;
                    playerView.getLayoutParams().height = homeRowHeight;
                }
            }
        }


        //=========客队球员(首发)=============
        final ArrayList<String> awayRowInfo = getRowCnt(awayTeamFormation);
        int awayRowCnt = awayRowInfo.size() + 1;

        final int awayRowHeight = height / 2 / awayRowCnt;
        for (int i = 0; i < awayRowCnt; i++) {
            TableRow tableRow = new TableRow(getContext());
            addView(tableRow);
            tableRow.getLayoutParams().height = awayRowHeight;

            if (i == awayRowCnt - 1) {
                MatchPlayerView goalKeeper = new MatchPlayerView(getContext());
                final PlayerInfo playerInfo = awayMainPlayerInfos.poll();
                if (playerInfo != null) {
                    final ArrayList<MatchTimelines> matchTimelinesArrayList = matchTimelines.get(playerInfo.playerId);
                    goalKeeper.setData(playerInfo, matchTimelinesArrayList, timeLineEventResMap);
                }
                tableRow.addView(goalKeeper);
                tableRow.setGravity(Gravity.CENTER_HORIZONTAL);

            } else {
                int awayCntPerRow =  Integer.parseInt(awayRowInfo.get(awayRowCnt - i - 2));
                for (int j = 0; j < awayCntPerRow; j++) {
                    MatchPlayerView playerView = new MatchPlayerView(getContext());
                    final PlayerInfo playerInfo = awayMainPlayerInfos.pollLast();
                    if (playerInfo != null) {
                        final ArrayList<MatchTimelines> matchTimelinesArrayList = matchTimelines.get(playerInfo.playerId);
                        playerView.setData(playerInfo, matchTimelinesArrayList, timeLineEventResMap);
                    }
                    tableRow.setWeightSum(awayCntPerRow);
                    tableRow.addView(playerView);
                    tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
                    ((LinearLayout.LayoutParams)playerView.getLayoutParams()).weight = 1;
                    playerView.getLayoutParams().height = homeRowHeight;
                    if (j == 0) {
                        ((LinearLayout.LayoutParams)playerView.getLayoutParams()).topMargin = UIUtils.dip2px(getContext(), 5);
                    }
                }
            }
        }
    }




    /**
     * 根据阵型得到行数
     * @param formation
     * @return
     */
    private ArrayList<String> getRowCnt(String formation) {
        ArrayList<String> res = new ArrayList<>();

        if (StringUtils.isEmptyString(formation)) {
            return res;
        }

        String[] arr = formation.split("-");
        if (arr.length == 0) {
            return res;
        }

        return new ArrayList<>(Arrays.asList(arr));
    }

}
