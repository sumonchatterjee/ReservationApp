package com.dineout.recycleradapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by vibhas.chandra on 07/10/16.
 */
public class StepsUploadAdapter extends RecyclerView.Adapter implements View.OnClickListener {
    private static final int ITEM_TYPE_HEADER = 1;
    private static final int ITEM_TYPE_STEPS = 2;
    private static final int ITEM_TYPE_DECLARATION = 3;
    OnSubmitButtonClickListener onSubmitButtonClickListener;

    private CheckBoxStateChangeCallback mCheckBoxStateChangeCallback;

    JSONObject response;
    JSONArray stepsToUpload=null;
    public StepsUploadAdapter(JSONObject jsonData){
        response=jsonData;
        if(response!=null){
            stepsToUpload=jsonData.optJSONArray("uploadSteps");
            //setJsonArray(stepsToUpload);
        }
    }

    private void inflateStepsToUpload(int position,StepsUploadViewHolder holder){
      if(holder!=null){
          int indexStepsArray=position-1;
          holder.getStepsDescriptionText().setText(stepsToUpload.optString(indexStepsArray));
          holder.getStepsNumberText().setText(Integer.toString(position));
      }
    }

    private void inflateStepsToUploadHeader(StepsHeaderViewHolder holder){
        if(holder!=null){
            holder.getStepsUploadHeaderText().setText("Steps to upload bill");
        }
    }

    private void inflateStepsToUploadDeclaration(StepsDeclarationViewHolder holder){
        if(holder!=null){
            holder.getCTA().setText(response.optString("cta"));
        }
    }


    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return ITEM_TYPE_HEADER;
        }
        else if(position==stepsToUpload.length()+1){
            return ITEM_TYPE_DECLARATION;
        }
        else {
            return ITEM_TYPE_STEPS;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType==ITEM_TYPE_STEPS){
            return new StepsUploadViewHolder(LayoutInflater.from(parent
                    .getContext())
                    .inflate(R.layout.upload_step_item, parent, false));
        }
        else if(viewType==ITEM_TYPE_HEADER){
            return new StepsHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_steps_title,parent,false));
        }
        else if(viewType==ITEM_TYPE_DECLARATION){
            return new StepsDeclarationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_steps_declaration,parent,false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == ITEM_TYPE_STEPS) {
            inflateStepsToUpload( position,(StepsUploadViewHolder) holder);
        }
        else if(holder.getItemViewType()== ITEM_TYPE_HEADER){
            inflateStepsToUploadHeader((StepsHeaderViewHolder)(holder));
        }
        else if(holder.getItemViewType()==ITEM_TYPE_DECLARATION) {
            inflateStepsToUploadDeclaration((StepsDeclarationViewHolder)(holder));
        }
    }

    @Override
    public int getItemCount() {

        return stepsToUpload==null ? 0 : stepsToUpload.length()+2;
    }

    @Override
    public void onClick(View v) {

    }

    public class StepsUploadViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView steps_number;
        TextView steps_description;
        public StepsUploadViewHolder(View itemView) {
            super(itemView);
            this.itemView=itemView;
            instantiateViews();
        }

        private void instantiateViews(){
           steps_description= (TextView) itemView.findViewById(R.id.steps_description);
            steps_number= (TextView) itemView.findViewById(R.id.step_numbers_text);

        }

        public TextView getStepsNumberText(){
            return steps_number;
        }

        public TextView getStepsDescriptionText(){
            return steps_description;
        }

    }

    public class StepsHeaderViewHolder extends RecyclerView.ViewHolder{
        View itemView;

        TextView steps_upload_header;
        public StepsHeaderViewHolder(View itemView) {
            super(itemView);
            this.itemView=itemView;
            instantiateViews();
        }

        private void instantiateViews(){
            steps_upload_header= (TextView) itemView.findViewById(R.id.steps_upload_header);


        }

        public TextView getStepsUploadHeaderText(){
            return steps_upload_header;
        }

    }

    public class StepsDeclarationViewHolder extends RecyclerView.ViewHolder{
        View itemView;

        CheckBox declarationCheckbox;
        Button cta_button;

        public StepsDeclarationViewHolder(View itemView) {
            super(itemView);
            this.itemView=itemView;
            instantiateViews();
        }

        private void instantiateViews(){
            cta_button= (Button) itemView.findViewById(R.id.done_steps_upload);
            declarationCheckbox = (CheckBox) itemView.findViewById(R.id.declaration_checkbox);
           cta_button.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   getOnSubmitButtonClickListener().onButtonCTAClicked(declarationCheckbox.isChecked());
               }
           });

            declarationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    getCheckBoxStateChangeCallback().declarationCheckBoxStatus(isChecked);
                }
            });
        }

        public CheckBox getStepsUploadCheckBox(){
            return declarationCheckbox;
        }
        public Button getCTA(){
            return cta_button;
        }

    }


    public void setOnSubmitButtonClickListener(OnSubmitButtonClickListener onSubmitButtonClickListener){
        this.onSubmitButtonClickListener=onSubmitButtonClickListener;
    }

    public OnSubmitButtonClickListener getOnSubmitButtonClickListener(){
        return onSubmitButtonClickListener;
    }

    public interface OnSubmitButtonClickListener {
        void onButtonCTAClicked(boolean isChecked);
    }


    public interface CheckBoxStateChangeCallback {
        void declarationCheckBoxStatus(boolean isTrue);
    }

    public void setCheckBoxStateChangeCallback(CheckBoxStateChangeCallback callback){
        this.mCheckBoxStateChangeCallback = callback;
    }

    private CheckBoxStateChangeCallback getCheckBoxStateChangeCallback() {
        return mCheckBoxStateChangeCallback;
    }
}
