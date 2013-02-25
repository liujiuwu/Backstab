package org.bone.backstab.snippet

import org.bone.backstab.session.PlurkAPIBox
import org.bone.soplurk.api.PlurkAPI

import net.liftweb.http.S

import net.liftweb.util.Helpers._
import net.liftweb.util.PassThru
import net.liftweb.util.Props

class PlurkAuthorization {

  def getPlurkAPI = {

    val callback = Props.get("plurk.callback", "http://localhost:8080/backstab")
    val appKey = Props.get("plurk.appKey", "yAW0goxD23qF")
    val appSecret = Props.get("plurk.appSecret", "agKpMI6qImQIzhJm11b3t9mvuZo7xpny")

    PlurkAPIBox.get.openOr(PlurkAPI.withCallback(appKey, appSecret, callback))

  }

  def redirectIfNotAuthorized(plurkAPI: PlurkAPI) {

    plurkAPI.OAuthUtils.checkToken.failed.foreach { exception =>

      PlurkAPIBox.set(Some(plurkAPI))

      val authURL = plurkAPI.getAuthorizationURL

      authURL.foreach { S.redirectTo }
      authURL.failed.foreach { 
        S.redirectTo("/", () => S.error("無法取得噗浪驗證網址，請稍候再試")) 
      }
    }
  }

  def render = {
    val plurkAPI = getPlurkAPI

    S.param("oauth_verifier").toOption match {
      case Some(code) => plurkAPI.authorize(code)
      case None => redirectIfNotAuthorized(plurkAPI)
    }

    PassThru
  }

}
