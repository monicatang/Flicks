package me.monicatang.flicks;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.monicatang.flicks.models.Config;
import me.monicatang.flicks.models.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    //list of movies
    ArrayList<Movie> movies;
    //config for image urls
    Config config;
    //context for rendering
    Context context;

    //initialize with list
    public MovieAdapter(ArrayList<Movie> movies) {
        this.movies = movies;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }


    //creates and inflates new view
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //create the view using the item_movie layout
        View movieView = inflater.inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(movieView);
    }

    //binds inflated view to new item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //get movie data at specified position
        Movie movie = movies.get(position);
        //populate view with movie data
        holder.tvTitle.setText(movie.getTitle());
        holder.tvOverview.setText(movie.getOverview());

        //determine current orientation
        boolean isPortrait = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        //build url for poster image
        String imageUrl = null;

        //if in portrait mode, load poster image
        if(isPortrait){
            imageUrl = config.getImageUrl(config.getPosterSize(), movie.getPosterPath());
        } else {
            //load backdrop image
            imageUrl = config.getImageUrl(config.getBackdropSize(), movie.getBackdropPath());
        }

        // get the correct placeholder and image view for current orientation
        int placeholderId = isPortrait ? R.drawable.flicks_movie_placeholder : R.drawable.flicks_backdrop_placeholder;
        ImageView imageView = isPortrait ? holder.ivPosterImage : holder.ivBackdropImage;

        int radius = 30; // corner radius, higher value = more rounded
        int margin = 10; // crop margin, set to 0 for corners with no crop

        //load image using Glide
        Glide.with(context)
                .load(imageUrl)

                .apply(
                        RequestOptions.placeholderOf(placeholderId)
                                .error(placeholderId)
                                .fitCenter()
                                .transform(new RoundedCornersTransformation(radius, margin))
                ).into(imageView);
    }

    //returns size of entire data set
    @Override
    public int getItemCount() {
        return movies.size();
    }


    //create the view holder as a static inner class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //track view objects
        @Nullable @BindView(R.id.ivPosterImage) ImageView ivPosterImage;
        @Nullable @BindView(R.id.ivBackdropImage) ImageView ivBackdropImage;
        @BindView(R.id.tvTitle) TextView tvTitle;
        @BindView(R.id.tvOverview) TextView tvOverview;

        @Override

        public void onClick(View view) {
            int position = getAdapterPosition();
            //ensure position is valid
            if(position != RecyclerView.NO_POSITION){
                //get movie at the position
                Movie movie = movies.get(position);
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                //serialize movie using parceler
                intent.putExtra(Movie.class.getSimpleName(), Parcels.wrap(movie));
                //show activity
                context.startActivity(intent);

            }
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //lookup view objects by id
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }
    }
}
