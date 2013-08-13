package com.rayrobdod.deductionTactics.ai

/**
 * TODO: Move to util
 * @author Raymond Dodge
 * @version 12 Feb 2012
 * @deprecated The HumanAI uses the actor model better so this is now unused by it.
 */
class WaitForAnyNotify(locks:Array[Object])
{
	val tg = new ThreadGroup("WaitForAnyNotify-" + WaitForAnyNotify.number)
	WaitForAnyNotify.number = WaitForAnyNotify.number + 1
	
	class WaitOnLock(lock:Object) extends Runnable {
		def run() = {
			lock.synchronized {lock.wait}
			WaitForAnyNotify.this.synchronized {WaitForAnyNotify.this.notifyAll}
		}
	}
	
	locks.zipWithIndex.foreach({(lock:Object, index:Int) =>
		new Thread(tg, new WaitOnLock(lock), "WaitForAnyNotifyThread-" + index).start()
	}.tupled)
}

object WaitForAnyNotify
{
	var number:Int = 0
}
