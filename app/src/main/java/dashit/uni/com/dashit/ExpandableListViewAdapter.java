package dashit.uni.com.dashit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jagrut on 17-Feb-16.
 */
public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private static final class ViewHolder {
        TextView textLabel;
    }

    private final List<HistoryFiles> itemList;
    private final LayoutInflater inflater;

    public ExpandableListViewAdapter(Context context, List<HistoryFiles> itemList) {
        this.inflater = LayoutInflater.from(context);
        this.itemList = itemList;
    }

    @Override
    public String getChild(int groupPosition, int childPosition) {

        return itemList.get(groupPosition).getFilesInDirectory().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemList.get(groupPosition).getFilesInDirectory().size();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             final ViewGroup parent) {
        View resultView = convertView;
        ViewHolder holder;


        if (resultView == null) {

            resultView = inflater.inflate(R.layout.list_item, null); //TODO change layout id
            holder = new ViewHolder();
            holder.textLabel = (TextView) resultView.findViewById(R.id.lblListItem); //TODO change view id
            resultView.setTag(holder);
        } else {
            holder = (ViewHolder) resultView.getTag();
        }

        final String item = getChild(groupPosition, childPosition);

        holder.textLabel.setText(item);

        return resultView;
    }

    @Override
    public HistoryFiles getGroup(int groupPosition) {
        return itemList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return itemList.size();
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View theConvertView, ViewGroup parent) {
        View resultView = theConvertView;
        ViewHolder holder;

        if (resultView == null) {
            resultView = inflater.inflate(R.layout.list_group, null); //TODO change layout id
            holder = new ViewHolder();
            holder.textLabel = (TextView) resultView.findViewById(R.id.lblListHeader); //TODO change view id
            resultView.setTag(holder);
        } else {
            holder = (ViewHolder) resultView.getTag();
        }

        final HistoryFiles item = getGroup(groupPosition);

        holder.textLabel.setText(item.getDirectory());

        return resultView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}