/*
 * Copyright (C) 2015 The JDCTeam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.jdcteam;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.settings.R;

/**
 * Created by antaresone on 23/07/15.
 */
 public class WeirdShit extends Activity {

    ImageView girl;
    ImageView mask1;
    ImageView mask2;
    ImageView mask3;
    ImageView higgs;
    int isHiggs = 0;
    Toast toast;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weird_shit);

        toast = new Toast(this);
        toast.makeText(this, R.string.toast_oncreate, Toast.LENGTH_SHORT).show();
        girl = (ImageView)findViewById(R.id.girl_pic);
        mask1 = (ImageView)findViewById(R.id.mask_1);
        mask2 = (ImageView)findViewById(R.id.mask_2);
        mask3 = (ImageView)findViewById(R.id.mask_3);
        higgs = (ImageView)findViewById(R.id.higgs_boson);

        mask1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHiggs = 1;
                String url = getString(R.string.url1);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        mask2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getString(R.string.url2);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        
        mask3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getString(R.string.url3);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRestart() {
        super.onRestart();
        switch ( isHiggs ) {
            case 0:
                toast = new Toast(this);
                toast.makeText(this, R.string.toast_info, Toast.LENGTH_LONG).show();
                break;
            case 1:
                girl.setVisibility(View.INVISIBLE);
                higgs.setVisibility(View.VISIBLE);
                mask1.setVisibility(View.GONE);
                mask2.setVisibility(View.GONE);
                mask3.setVisibility(View.GONE);
                toast = new Toast(this);
                toast.makeText(this, R.string.toast_higgs, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
