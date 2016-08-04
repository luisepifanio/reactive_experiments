import rx.Observable
import service.ItemService

import java.util.concurrent.TimeUnit

println 'Running Reactive Experiments'


ItemService itemService = new ItemService();

itemService.getByIdAsync("MLC435153885")
        .timeout(7, TimeUnit.SECONDS,Observable.just(null))
        .subscribe(System.out.&println);

def readln = javax.swing.JOptionPane.&showInputDialog
def username = readln 'Enter to fisnish'
