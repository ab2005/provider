/*
 * Copyright (C) 2015 Seagate LLC
 */

package com.seagate.alto;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.android.alto.R;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Provides UI for the Detail page with Collapsing Toolbar.
 */
public class DetailActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set Collapsing Toolbar layout to the screen
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        // Set title of Detail page
        collapsingToolbar.setTitle(getString(R.string.item_title));

        SimpleDraweeView sdv = (SimpleDraweeView) findViewById(R.id.image);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (b != null) {
            int index = b.getInt(PlaceholderContent.INDEX);
            sdv.setImageURI(PlaceholderContent.getUri(index));
        }

    }
}
