import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO

scan('30 seconds')

def LOG_PATH = 'logs'
def LOG_ARCHIVE = "${LOG_PATH}/archive"

///////////Pattern Configuration//////////////////
// %d{yyyy-MM-dd HH:mm:ss} [%thread] %level %logger{35} - %msg%n
// %date{"yyyy-MM-dd'T'HH:mm:ss,SSSXXX", UTC}
// Java 6
// String dateFormat   = '[eventdate:%d{yyyy-MM-dd}T%d{HH:mm:ss.SSS}]'
// JDK 7
String dateFormat       = '[eventdate:%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSXXX}]'
String levelFormat      = '[level:%level]'
String threadFormat     = '[thread:%thread]'
String categoryFormat   = ''                         // '[category:%c{2}]'

String applicationConversionPattern = "$dateFormat $levelFormat $threadFormat $categoryFormat %msg%n"
        .toString()
        .replaceAll("\\s+", " ")
        .trim();
/////////////////END OF Pattern Configuration/////


appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = applicationConversionPattern
    }
}
appender('FILE', FileAppender) {
    file = "${LOG_PATH}/logfile.log"
    encoder(PatternLayoutEncoder) {
        pattern = applicationConversionPattern
        outputPatternAsHeader = true
    }
}

appender("ROLLINGFILE", RollingFileAppender) {
    file = "${LOG_PATH}/rollingfile.log"
    rollingPolicy(SizeAndTimeBasedRollingPolicy) {
        fileNamePattern = "${LOG_ARCHIVE}/rollingfile.%d{yyyy-MM-dd}.%i.log"
        maxFileSize = '2MB'
        maxHistory = 4
        totalSizeCap = new FileSize(10 * 1024 * 1024)
        cleanHistoryOnStart = false
    }
    encoder(PatternLayoutEncoder) {
        pattern = applicationConversionPattern
    }
}
appender('ASYNC', AsyncAppender) {
    //appenderRef("ROLLINGFILE")
    appenderRef('STDOUT')
    queueSize = 512
    discardingThreshold = 0
    maxFlushTime = 10_000 // Cortamos luego de 10 segundos
}

//logger("guru.springframework.blog.logbackgroovy", INFO, ['STDOUT', 'FILE', 'ASYNC'], false)
root(DEBUG, ['ASYNC'])
