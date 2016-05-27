datasource {
	jdbc.driver   = "oracle.jdbc.OracleDriver"
	jdbc.url      = "jdbc:oracle:thin:@192.168.1.16:1521:payment"
	jdbc.user     = "zft_v3_dev"

	jdbc.password = "zft_v3_dev"
	pool.MinIdle                            = 2
	pool.MaxActive                          = 10
	pool.MaxWait                            = 20000
	pool.TestWhileIdle                      = true
	pool.TimeBetweenEvictionRunsMillis      = 60000
	pool.MinEvictableIdleTimeMillis         = 1800000
	pool.ValidationQuery                    = 'select 1 from dual'
}

commons {
	register.position = true
	reqTimeCheck = false
	defaulAgencyCode = '1000'
	maxFileUpload = 1024 * 1024 * 2
}

path {
	signature = "webroot/uploads/sign/"
	merchat = "webroot/uploads/signature/"
	temp = "webroot/tmp_upload/"
	banner = "webroot/uploads/banner/"
}

url {
	message_receiver = "http://172.16.1.221:9090/certauth/notify.do"
	message = "http://172.16.1.221:9090/news/doNewsPost.do"
	message_notify_url = "http://branchbts.21er.net:15080/html/smssearch.html"
	message_auth = "http://106.37.206.154:12862/handler"
	message_auth_merchant_id="354"
}

controller {
	is_send_message = false
	is_card_validate = true
}