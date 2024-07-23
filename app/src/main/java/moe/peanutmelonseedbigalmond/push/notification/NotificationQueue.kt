package moe.peanutmelonseedbigalmond.push.notification

import java.util.concurrent.ArrayBlockingQueue

class NotificationQueue(capacity:Int) {
    private val queue=ArrayBlockingQueue<String>(capacity)

    val size:Int
        get() = queue.size

    fun offer(e:String){
        if (!queue.offer(e)){
            queue.poll()
        }
        queue.offer(e)
    }

    fun poll(){
        queue.poll()
    }

    fun clear(){
        queue.clear()
    }

    fun toArray():Array<String>{
        return queue.toTypedArray()
    }

    operator fun iterator():Iterator<String>{
        return queue.iterator()
    }
}