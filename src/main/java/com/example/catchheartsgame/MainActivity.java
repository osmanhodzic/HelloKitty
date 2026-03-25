package com.example.catchheartsgame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    class GameView extends SurfaceView implements SurfaceHolder.Callback {

        private GameThread thread;
        private Bitmap playerBitmap, heartBitmap;
        private int playerX, playerY, playerWidth, playerHeight;
        private ArrayList<Heart> hearts = new ArrayList<>();
        private int screenWidth, screenHeight;
        private Random random = new Random();
        private boolean gameOver = false;
        private Rect restartButton;

        private int score = 0;

        public GameView(MainActivity context) {
            super(context);
            getHolder().addCallback(this);
            thread = new GameThread(getHolder(), this);
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            screenWidth = getWidth();
            screenHeight = getHeight();

            // velicina
            Bitmap tempPlayer = BitmapFactory.decodeResource(getResources(), R.drawable.player);
            playerWidth = screenWidth / 5;
            playerHeight = playerWidth;
            playerBitmap = Bitmap.createScaledBitmap(tempPlayer, playerWidth, playerHeight, false);

            Bitmap tempHeart = BitmapFactory.decodeResource(getResources(), R.drawable.heart);
            int heartSize = screenWidth / 8;
            heartBitmap = Bitmap.createScaledBitmap(tempHeart, heartSize, heartSize, false);

            restartGame();

            thread.setRunning(true);
            thread.start();
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

        }

        private void restartGame() {
            playerX = screenWidth / 2 - playerWidth / 2;
            playerY = screenHeight - playerHeight - 50;
            hearts.clear();
            score = 0;
            gameOver = false;

            int buttonWidth = screenWidth / 2;
            int buttonHeight = 150;
            int left = screenWidth / 2 - buttonWidth / 2;
            int top = screenHeight / 2 + 50;
            restartButton = new Rect(left, top, left + buttonWidth, top + buttonHeight);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (gameOver) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (restartButton.contains((int) event.getX(), (int) event.getY())) {
                        restartGame();
                    }
                }
            } else {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    playerX = (int) event.getX() - playerWidth / 2;
                    if (playerX < 0) playerX = 0;
                    if (playerX > screenWidth - playerWidth) playerX = screenWidth - playerWidth;
                }
            }
            return true;
        }

        public void update() {
            if (gameOver) return;

            if (random.nextInt(60) == 0) {
                int x = random.nextInt(screenWidth - heartBitmap.getWidth());
                hearts.add(new Heart(x, -heartBitmap.getHeight()));
            }

            ArrayList<Heart> removeList = new ArrayList<>();
            for (Heart h : hearts) {
                h.y += 10;

                Rect playerRect = new Rect(playerX, playerY, playerX + playerWidth, playerY + playerHeight);
                Rect heartRect = new Rect(h.x, h.y, h.x + heartBitmap.getWidth(), h.y + heartBitmap.getHeight());

                if (Rect.intersects(playerRect, heartRect)) {
                    score++;
                    removeList.add(h);
                } else if (h.y > screenHeight) {
                    gameOver = true;
                }
            }
            hearts.removeAll(removeList);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (canvas == null) return;

            canvas.drawRGB(255, 255, 255); // pozadina
            canvas.drawBitmap(playerBitmap, playerX, playerY, null);

            for (Heart h : hearts) {
                canvas.drawBitmap(heartBitmap, h.x, h.y, null);
            }

            Paint paint = new Paint();
            paint.setTextSize(60);
            paint.setColor(android.graphics.Color.BLACK);
            canvas.drawText("Score: " + score, 50, 100, paint);

            if (gameOver) {
                paint.setTextSize(120);
                paint.setColor(android.graphics.Color.RED);
                canvas.drawText("Izgubili ste :(", screenWidth / 4, screenHeight / 2, paint);

                // dugme restart
                paint.setTextSize(60);
                paint.setColor(android.graphics.Color.BLUE);
                canvas.drawRect(restartButton, paint);
                paint.setColor(android.graphics.Color.WHITE);
                canvas.drawText("Restart", restartButton.left + 50, restartButton.centerY() + 20, paint);
            }
        }

        class Heart {
            int x, y;

            Heart(int x, int y) {
                this.x = x;
                this.y = y;
            }
        }

        class GameThread extends Thread {
            private SurfaceHolder surfaceHolder;
            private GameView gameView;
            private boolean running = false;

            public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
                this.surfaceHolder = surfaceHolder;
                this.gameView = gameView;
            }

            public void setRunning(boolean running) {
                this.running = running;
            }

            @Override
            public void run() {
                Canvas canvas;
                while (running) {
                    canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas();
                        synchronized (surfaceHolder) {
                            gameView.update();
                            gameView.draw(canvas);
                        }
                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }

                    try {
                        sleep(16);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}