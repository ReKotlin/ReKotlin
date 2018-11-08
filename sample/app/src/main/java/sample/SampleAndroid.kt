package sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.rekotlin.StoreSubscriber

class MainActivity : AppCompatActivity(), StoreSubscriber<GameState> {
    lateinit var ctTxtView: TextView
    lateinit var button: Button

    override fun newState(state: GameState) {
        ctTxtView.text = "ct = ${state.ct}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ctTxtView = findViewById(R.id.txt_ct)
        button = findViewById(R.id.button)
        button.setOnClickListener {
            gameStore.dispatch(ButtonClickAction())
        }
        gameStore.subscribe(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        gameStore.unsubscribe(this)
    }
}