#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import endpoints
import gdata.photos.service
import gdata.media
import gdata.geo
import atom
import logging
import base64
from cStringIO import StringIO

from protorpc import remote
from protorpc import message_types
from protorpc import messages
from google.appengine.api import mail
from google.appengine.ext import ndb
from datetime import datetime
from datetime import timedelta
#
# NOTA BENE:
# SONO STATI COMMENTATI TUTTI I RIFERIMENTI A MESE E ANNO. ATTENTO!!!!
#
# <!----------- DA MODIFICARE PRIMA DI CARICARE SUL PROGETTO ORIGINALE ----------!>
WEB_CLIENT_ID = '803782534144-6n7lv3ii2a49le1q8arifv5cltglj4em.apps.googleusercontent.com'
ANDROID_CLIENT_ID = '803782534144-ldi6pbntu4b298061c4a4anrldlj34ai.apps.googleusercontent.com'
ANDROID_AUDIENCE = WEB_CLIENT_ID
USERNAME = 'arteverywhere00@gmail.com'
MONTHS = ['Gennaio', 'Febbraio', 'Marzo', 'Aprile', 'Maggio', 'Giugno',
          'Luglio', 'Agosto', 'Settembre', 'Ottobre', 'Novembre', 'Dicembre']


# Datastore Tables
class TechniqueEntry(ndb.Model):
    nome = ndb.StringProperty(required=True)
    eng = ndb.StringProperty(required=True)

class PictureEntry(ndb.Model):
    # DECOMMENTARE ANNO E MESE
    url = ndb.StringProperty(required=True)
    photo_id = ndb.StringProperty(required=True)
    date_time = ndb.DateTimeProperty(auto_now_add=True)
    title = ndb.StringProperty(required=True)
    artist = ndb.StringProperty(required=True)
    descr = ndb.StringProperty()
    luogo = ndb.StringProperty()
    dim = ndb.StringProperty()
    likes = ndb.IntegerProperty()
    technique = ndb.KeyProperty(kind=TechniqueEntry)
    anno = ndb.IntegerProperty()
    mese = ndb.StringProperty()

class ArtistEntry(ndb.Model):
    email = ndb.StringProperty(required=True)
    nome = ndb.StringProperty(required=True)
    cognome = ndb.StringProperty(required=True)
    nickname = ndb.StringProperty()
    sito = ndb.StringProperty()
    bio = ndb.StringProperty()
    pic = ndb.StringProperty(required=True)
    photo_id = ndb.StringProperty()
    ban_counter = ndb.IntegerProperty()

class CommentEntry(ndb.Model):
    comment = ndb.StringProperty(required=True)
    email = ndb.StringProperty(required=True)
    id = ndb.StringProperty()
    date_time = ndb.DateTimeProperty(auto_now_add=True)

class LikeEntry(ndb.Model):
    url = ndb.StringProperty(required=True)
    email = ndb.StringProperty(required=True)
    date_time = ndb.DateTimeProperty(auto_now_add=True)

class BlackListEntry(ndb.Model):
    email = ndb.StringProperty(required=True)

class FlagEntry(ndb.Model):
    flagger = ndb.StringProperty(required=True)
    flagged = ndb.StringProperty(required=True)
    content = ndb.StringProperty(required=True)
'''
class FollowEntry(ndb.Model):
    follower = ndb.StringProperty(required=True)
    followed = ndb.StringProperty(required=True)
'''

# Message Classes
class DefaultResponseMessage(messages.Message):
    message = messages.StringField(1)

class UploadRequestMessage(messages.Message):
    # DECOMMENTARE ANNO E MESE
    title = messages.StringField(1, required=True)
    image = messages.StringField(2, required=True)
    artist = messages.StringField(3, required=True)
    technique = messages.StringField(4, required=True)
    descr = messages.StringField(5)
    luogo = messages.StringField(6)
    dim = messages.StringField(7)
    anno = messages.IntegerField(8)
    mese = messages.StringField(9)
    admin = messages.StringField(10)

class DeleteRequestMessage(messages.Message):
    url = messages.StringField(1, required=True)
    admin = messages.StringField(2)

class PictureUpdateMessage(messages.Message):
    # DECOMMENTARE ANNO E MESE
    url = messages.StringField(1, required=True)
    new_title = messages.StringField(2)
    new_technique = messages.StringField(3)
    new_descr = messages.StringField(4)
    new_luogo = messages.StringField(5)
    new_dim = messages.StringField(6)
    new_anno = messages.IntegerField(7)
    new_mese = messages.StringField(8)
    admin = messages.StringField(9)

class ArtistInfoMessage(messages.Message):
    email = messages.StringField(1, required=True)
    nome = messages.StringField(2, required=True)
    cognome = messages.StringField(3, required=True)
    nickname = messages.StringField(4)
    sito = messages.StringField(5)
    bio = messages.StringField(6)
    pic = messages.StringField(7, required=True)

class ArtistUpdateMessage(messages.Message):
    email = messages.StringField(1, required=True)
    new_email = messages.StringField(2)
    new_nome = messages.StringField(3)
    new_cognome = messages.StringField(4)
    new_nickname = messages.StringField(5)
    new_sito = messages.StringField(6)
    new_bio = messages.StringField(7)
    new_pic = messages.StringField(8)
    new_ban_counter = messages.IntegerField(9)
    admin = messages.StringField(10)

class UpdateResponseMessage(messages.Message):
    message = messages.StringField(1, required=True)
    url = messages.StringField(2)

class ArtistRequestMessage(messages.Message):
    email = messages.StringField(1, required=True)

class ArtistDetailsMessage(messages.Message):
    message = messages.StringField(1)
    email = messages.StringField(2)
    nome = messages.StringField(3)
    cognome = messages.StringField(4)
    nickname = messages.StringField(5)
    sito = messages.StringField(6)
    bio = messages.StringField(7)
    pic = messages.StringField(8)

class PictureDetailsMessage(messages.Message):
    title = messages.StringField(1, required=True)
    date_time = messages.StringField(2, required=True)
    url = messages.StringField(3, required=True)
    artist = messages.StringField(4, required=True)
    technique = messages.StringField(5)
    descr = messages.StringField(6)
    luogo = messages.StringField(7)
    dim = messages.StringField(8)
    likes = messages.IntegerField(9)
    anno = messages.IntegerField(10)
    mese = messages.StringField(11)

class DownloadRequestMessage(messages.Message):
    fetch = messages.IntegerField(1, required=True)
    date_time = messages.StringField(2)
    lang = messages.StringField(3)

class DownloadResponseMessage(messages.Message):
    # DECOMMENTARE ANNO E MESE
    title = messages.StringField(1, required=True)
    date_time = messages.StringField(2, required=True)
    url = messages.StringField(3, required=True)
    artist = messages.StringField(4, required=True)
    technique = messages.StringField(5)
    descr = messages.StringField(6)
    luogo = messages.StringField(7)
    dim = messages.StringField(8)
    likes = messages.IntegerField(9)
    anno = messages.IntegerField(10)
    mese = messages.StringField(11)
    liked = messages.StringField(12)  # USATO DA LIKED_ARTWORKS

class DownloadResponseCollection(messages.Message):
    photos = messages.MessageField(DownloadResponseMessage, 1, repeated=True)
    message = messages.StringField(2)

class PictureDetailsCollection(messages.Message):
    photos = messages.MessageField(PictureDetailsMessage, 1, repeated=True)
    message = messages.StringField(2)

class RefreshRequestMessage(messages.Message):
    fetch = messages.IntegerField(1, required=True)
    date_time = messages.StringField(2, required=True)
    technique = messages.StringField(3)
    luogo = messages.StringField(4)
    lang = messages.StringField(5)


class TechniqueInsertMessage(messages.Message):
    technique = messages.StringField(1, required=True)
    eng = messages.StringField(2, required=True)

class TechniqueRequestMessage(messages.Message):
    lang = messages.StringField(1)

class TechniqueResponseMessage(messages.Message):
    technique = messages.StringField(1, required=True)

class TechniqueResponseCollection(messages.Message):
    techniques = messages.MessageField(TechniqueResponseMessage, 1, repeated=True)

class InsertCommentMessage(messages.Message):
    comment = messages.StringField(1, required=True)
    url = messages.StringField(2, required=True)
    email = messages.StringField(3)
    admin = messages.StringField(4)

class DeleteCommentMessage(messages.Message):
    id = messages.StringField(1, required=True)

class CommentListMessage(messages.Message):
    url = messages.StringField(1, required=True)
    fetch = messages.IntegerField(2)

class CommentDetails(messages.Message):
    comment = messages.StringField(1, required=True)
    email = messages.StringField(2, required=True)
    id = messages.StringField(3, required=True)
    pic = messages.StringField(4)
    name = messages.StringField(5)
    date = messages.StringField(6, required=True)

class CommentDetailsCollection(messages.Message):
    comments = messages.MessageField(CommentDetails, 1, repeated=True)
    message = messages.StringField(2)

class GeneralLikeRequest(messages.Message):
    url = messages.StringField(1, required=True)
    email = messages.StringField(2, required=True)
    admin = messages.StringField(3)

class GeneralLikeResponse(messages.Message):
    hasLiked = messages.BooleanField(1, required=True)
    likes = messages.IntegerField(2, required=True)
    message = messages.StringField(3)

class LikeListRequest(messages.Message):
    url = messages.StringField(1, required=True)

class LikeDetailsResponse(messages.Message):
    email = messages.StringField(1, required=True)
    name = messages.StringField(2)
    pic = messages.StringField(3)

class LikedArtworksRequest(messages.Message):
    email = messages.StringField(1, required=True)
    date_time = messages.StringField(2)
    fetch = messages.IntegerField(3)

class LikeDetailsCollection(messages.Message):
    likes = messages.MessageField(LikeDetailsResponse, 1, repeated=True)
    message = messages.StringField(2)

class TechniqueSearchRequest(messages.Message):
    fetch = messages.IntegerField(1, required=True)
    date_time = messages.StringField(2)
    technique = messages.StringField(3, required=True)
    lang = messages.StringField(4)

class PlaceSearchRequest(messages.Message):
    fetch = messages.IntegerField(1, required=True)
    date_time = messages.StringField(2)
    luogo = messages.StringField(3, required=True)
    lang = messages.StringField(4)

class ArtistBriefMessage(messages.Message):
    email = messages.StringField(1, required=True)
    nome = messages.StringField(2, required=True)
    cognome = messages.StringField(3, required=True)
    nickname = messages.StringField(4)
    pic = messages.StringField(5)

class ArtistBriefCollection(messages.Message):
    artists = messages.MessageField(ArtistBriefMessage, 1, repeated=True)
    message = messages.StringField(2)

class FlagRequestMessage(messages.Message):
    flagger = messages.StringField(1, required=True)
    flagged = messages.StringField(2, required=True)
    code = messages.IntegerField(3, required=True)
    url = messages.StringField(4, required=True)
    comment = messages.StringField(5)

class BlockedUserRequest(messages.Message):
    email = messages.StringField(1, required=True)

class BlocekedUserResponse(messages.Message):
    is_blocked = messages.BooleanField(1, required=True)

class FlagListRequest(messages.Message):
    email = messages.StringField(1, required=True)

class FlagEntryMessage(messages.Message):
    flagger = messages.StringField(1, required=True)
    content = messages.StringField(2, required=True)

class FlagEntryCollection(messages.Message):
    flags = messages.MessageField(FlagEntryMessage, 1, repeated=True)
'''
class FollowRequestMessage(messages.Message):
    follower = messages.StringField(1, required=True)
    followed = messages.StringField(2, required=True)

class FollowedRequestMessage(messages.Message):
    follower = messages.StringField(1, required=True)
'''

@endpoints.api(name="artEverywhere", version="v1",
               allowed_client_ids=[WEB_CLIENT_ID, ANDROID_CLIENT_ID,
                                   endpoints.API_EXPLORER_CLIENT_ID],
               audiences=[ANDROID_AUDIENCE],
               scopes=[endpoints.EMAIL_SCOPE])
class PicasaWA(remote.Service):
    album_id = None

    def __init__(self):
        gd_client = PicasaWA.login()

        albums = gd_client.GetUserFeed(user=USERNAME)
        for album in albums.entry:
            if album.title.text == 'ArtEverywhere':
                PicasaWA.album_id = album.gphoto_id.text

        if PicasaWA.album_id is None:
            PicasaWA.album_id = gd_client.InsertAlbum(title='ArtEverywhere', summary='ArtEverywhere').gphoto_id.text

    @endpoints.method(UploadRequestMessage, DefaultResponseMessage,
                      path='upload', http_method="POST", name="upload.putphoto")
    def upload_putphoto(self, request):
        # CONTROLLO USER BLOCCATO
        if request.admin != "googleworkshop":
            blocked = BlackListEntry.query(BlackListEntry.email == request.artist.lower()).get()

            if blocked is not None:
                return DefaultResponseMessage(message="Artist is Blocked!")

        artist = ArtistEntry.query(ArtistEntry.email == request.artist.lower()).get()
        if artist is None:
            return DefaultResponseMessage(message="Artist not found!")

        technique = TechniqueEntry.query(ndb.OR(TechniqueEntry.nome == request.technique,
                                                TechniqueEntry.eng == request.technique)).get()
        if technique is None:
            return DefaultResponseMessage(message="Technique not found!")
        # DECOMMENTARE ANNO E MESE

        if request.mese is not None:
            if request.mese not in MONTHS:
                return DefaultResponseMessage(message="Bad Mese found!")

        if request.anno is not None:
            if request.anno < 0:
                return DefaultResponseMessage(message="Bad Anno found!")

        decoded_img_str = base64.decodestring(request.image)
        imgfile = StringIO(decoded_img_str)

        gd_client = PicasaWA.login()
        album_url = '/data/feed/api/user/%s/albumid/%s' % (USERNAME, PicasaWA.album_id)

        metadata = gdata.photos.PhotoEntry()
        metadata.title = atom.Title(text=request.title)
        tags = '%s, %s, %s' % (request.artist.lower(), request.technique, request.luogo)
        keywords = gdata.media.Keywords()
        keywords.text = tags
        metadata.media.keywords = keywords

        photo = gd_client.InsertPhoto(album_url, metadata, imgfile, content_type='image/jpeg')
        # AGGIUNGERE ANNO E MESE
        pic = PictureEntry(parent=artist.key, title=request.title, url=photo.content.src,
                           descr=request.descr, luogo=request.luogo, artist=artist.email,
                           dim=request.dim, likes=0, technique=technique.key, photo_id=photo.gphoto_id.text,
                           anno=request.anno, mese=request.mese)
        pic.put()
        return DefaultResponseMessage(message="Artwork added!")

    @endpoints.method(DeleteRequestMessage, DefaultResponseMessage,
                      path='delete', http_method='GET', name='delete.deletephoto')
    def delete_deletephoto(self, request):
        pic = PictureEntry.query(PictureEntry.url == request.url).get()

        if pic is None:
            return DefaultResponseMessage(message="Artwork not found!")
        # CONTROLLO USER BLOCCATO
        if request.admin != "googleworkshop":
            blocked = BlackListEntry.query(BlackListEntry.email == pic.artist).get()

            if blocked is not None:
                return DefaultResponseMessage(message="Artist is Blocked!")

        comments = CommentEntry.query(ancestor=pic.key)

        for comment in comments:
            comment.key.delete()

        likes = LikeEntry.query(ancestor=pic.key)

        for like in likes:
            like.key.delete()

        gd_client = PicasaWA.login()

        photos = gd_client.GetFeed('/data/feed/api/user/%s/albumid/%s?kind=photo' % (USERNAME, PicasaWA.album_id))
        for photo in photos.entry:
            if photo.gphoto_id.text == pic.photo_id:
                gd_client.Delete(photo)

        pic.key.delete()

        return DefaultResponseMessage(message="Artwork removed!")

    @endpoints.method(PictureUpdateMessage, DefaultResponseMessage,
                      path='picinfo', http_method='GET', name='picinfo.updatepicture')
    def update_picture(self, request):
        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return DefaultResponseMessage(message="Artwork not found!")

        if request.admin != "googleworkshop":
            blocked = BlackListEntry.query(BlackListEntry.email == photo.artist).get()

            if blocked is not None:
                return DefaultResponseMessage(message="Artist is Blocked!")

        gd_client = PicasaWA.login()
        photo_entry = None

        photos = gd_client.GetFeed('/data/feed/api/user/%s/albumid/%s?kind=photo' % (USERNAME, PicasaWA.album_id))
        for pic in photos.entry:
            if photo.photo_id == pic.gphoto_id.text:
                photo_entry = pic
                break

        if request.new_title is not None:
            photo.title = request.new_title
            if photo_entry.title.text is not None:
                photo_entry.title.text = request.new_title
            else:
                photo_entry.title = atom.Title(text=request.new_title)
        if request.new_descr is not None:
            photo.descr = request.new_descr
        if request.new_luogo is not None:
            photo.luogo = request.new_luogo
            technique = TechniqueEntry.query(TechniqueEntry.key == photo.technique).get()
            tags = '%s, %s, %s' % (photo.artist, technique.nome, request.new_luogo)
            if photo_entry.media.keywords.text is not None:
                photo_entry.media.keywords.text = tags
            else:
                keywords = gdata.media.Keywords()
                keywords.text = tags
                photo_entry.media.keywords = keywords
        if request.new_dim is not None:
            photo.dim = request.new_dim
        if request.new_technique is not None:
            technique = TechniqueEntry.query(ndb.OR(TechniqueEntry.nome == request.new_technique,
                                                    TechniqueEntry.eng == request.new_technique)).get()
            if technique is not None:
                photo.technique = technique.key
                tags = '%s, %s, %s' % (photo.artist, technique.nome, photo.luogo)
                if photo_entry.media.keywords.text is not None:
                    photo_entry.media.keywords.text = tags
                else:
                    keywords = gdata.media.Keywords()
                    keywords.text = tags
                    photo_entry.media.keywords = keywords
            else:
                return DefaultResponseMessage(message="Technique not found!")
        # DECOMMENTARE ANNO E MESE

        if request.new_anno is not None:
            if request.new_anno < 0:
                return DefaultResponseMessage(message="Bad Anno Found!")
            photo.anno = request.new_anno
        if request.new_mese is not None:
            if request.new_mese not in MONTHS:
                return DefaultResponseMessage(message="Bad Mese Found!")
            photo.mese = request.new_mese

        gd_client.UpdatePhotoMetadata(photo_entry)
        photo.put()

        return DefaultResponseMessage(message="Artwork Updated!")

    @endpoints.method(ArtistInfoMessage, DefaultResponseMessage,
                      path='registration', http_method='POST', name='registration.registerartist')
    def register_artist(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email.lower()).get()

        if artist is not None:
            return DefaultResponseMessage(message="Artist already exists!")

        email = request.email
        nome = request.nome
        cognome = request.cognome
        nickname = request.nickname
        sito = request.sito
        bio = request.bio
        pic = request.pic

        artist = ArtistEntry(email=email.lower(), nome=nome, cognome=cognome,
                             nickname=nickname, sito=sito, bio=bio, pic=pic, ban_counter=0)

        artist.put()
        message = mail.EmailMessage(sender="arteverywhere00@gmail.com",
                                    subject="Grazie per esserti registrato ad Art Everywhere!!")

        message.to = '%s %s <%s>' % (nome, cognome, email)
        message.body = """
        Caro %s:

        Il tuo account di Art Everywhere e' stato attivato. Ora puoi usare
        l'app per mobile e accedere usando il tuo account Google
        per utilizzare le funzionalita' dell'app.

        Metti mi piace sulla nostra pagina di Facebook, per scoprire
        tutti i nostri aggiornamenti ed essere aggiornato in tempo reale
        sulle nuove funzionalita di Art Everywhere:
        https://www.facebook.com/ArtEverywhereApp

        Per ogni domanda o suggerimento, sentiti libero di contattarci.

        Il team di Art Everywhere.
        """ % nome

        message.html = """
        <html>
            <head></head>
            <body>
                <p align="center"><img alt="" height="100" src="https://lh4.googleusercontent.com/-4EUhV6V6U9Y/VN24VjX-oDI/AAAAAAAAABw/I9ZVh-fYSXs/s798-no/ae.png" width="100"></p>
                <hr>

                <p>Caro %s:</p>
                <div>Il tuo account di Art Everywhere e' stato attivato.</div>
                <div>Ora puoi usare l'app per mobile e accedere usando il tuo account Google
                per utilizzare le funzionalita' dell'app.</div><br\>

                <div>Metti mi piace sulla nostra pagina di Facebook, per scoprire
                tutti i nostri aggiornamenti ed essere aggiornato in tempo reale
                sulle nuove funzionalita di Art Everywhere:</div>
                <div>https://www.facebook.com/ArtEverywhereApp</div><br\>

                <div>Per ogni domanda o suggerimento, sentiti libero di contattarci.</div>

                <p>Il team di Art Everywhere.</p>
            </body>
        </html>
        """ % nome

        message.send()

        return DefaultResponseMessage(message="Artist registered!")

    @endpoints.method(ArtistUpdateMessage, UpdateResponseMessage,
                      path='artistinfo', http_method='POST', name='artistinfo.updateartist')
    def update_artist(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email.lower()).get()
        # CONTROLLO USER BLOCCATO
        if request.admin != "googleworkshop":
            blocked = BlackListEntry.query(BlackListEntry.email == request.email.lower()).get()

            if blocked is not None:
                return UpdateResponseMessage(message="Artist is Blocked!")

        if artist is None:
            return UpdateResponseMessage(message="Artist not found!")

        url = None

        if request.new_email is not None:
            artist.email = request.new_email.lower()
        if request.new_nome is not None:
            artist.nome = request.new_nome
        if request.new_cognome is not None:
            artist.cognome = request.new_cognome
        if request.new_nickname is not None:
            artist.nickname = request.new_nickname
        if request.new_sito is not None:
            artist.sito = request.new_sito
        if request.new_bio is not None:
            artist.bio = request.new_bio
        if request.new_pic is not None:
            decoded_img_str = base64.decodestring(request.new_pic)
            imgfile = StringIO(decoded_img_str)
            gd_client = PicasaWA.login()

            if artist.photo_id is not None:
                photos = gd_client.GetFeed('/data/feed/api/user/%s/albumid/%s?kind=photo' % (USERNAME, PicasaWA.album_id))
                for photo in photos.entry:
                    if photo.gphoto_id.text == artist.photo_id:
                        gd_client.Delete(photo)

            album_url = '/data/feed/api/user/%s/albumid/%s' % (USERNAME, PicasaWA.album_id)

            metadata = gdata.photos.PhotoEntry()
            metadata.title = atom.Title(text="%s's Profile Pic" % artist.email)
            tags = '%s, %s' % (artist.email, "profilepic")
            keywords = gdata.media.Keywords()
            keywords.text = tags
            metadata.media.keywords = keywords
            photo = gd_client.InsertPhoto(album_url, metadata, imgfile, content_type='image/jpeg')
            artist.pic = photo.content.src
            artist.photo_id = photo.gphoto_id.text
            url = photo.content.src
        if request.new_ban_counter is not None:
            artist.ban_counter = request.new_ban_counter

        artist.put()
        if url is not None:
            return UpdateResponseMessage(message="Artist updated!", url=url)
        else:
            return UpdateResponseMessage(message="Artist updated!")

    @endpoints.method(ArtistRequestMessage, ArtistDetailsMessage,
                      path='getinfo', http_method='GET', name='getinfo.getartist')
    def get_artist(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email.lower()).get()

        if artist is None:
            return ArtistDetailsMessage(message="Artist Not Found!")

        return ArtistDetailsMessage(email=artist.email, nome=artist.nome,
                                    cognome=artist.cognome, nickname=artist.nickname,
                                    sito=artist.sito, bio=artist.bio, pic=artist.pic)

    @endpoints.method(TechniqueRequestMessage, TechniqueResponseCollection,
                      path='techniques', http_method='GET', name='techniques.gettechniques')
    def get_techniques(self, request):

        if request.lang == 'ENG':
            techniques = TechniqueEntry.query().order(TechniqueEntry.eng)
        else:
            techniques = TechniqueEntry.query().order(TechniqueEntry.nome)

        messlist = []

        for technique in techniques:
            if request.lang == 'ENG':
                messlist.append(TechniqueResponseMessage(technique=technique.eng))
            else:
                messlist.append(TechniqueResponseMessage(technique=technique.nome))

        return TechniqueResponseCollection(techniques=messlist)

    @endpoints.method(TechniqueInsertMessage, DefaultResponseMessage,
                      path='technique', http_method='GET', name='technique.puttechnique')
    def put_technique(self, request):
        entry = TechniqueEntry.query(TechniqueEntry.nome == request.technique).get()
        if entry is not None:
            return DefaultResponseMessage(message="Technique already existent!")

        technique = TechniqueEntry(nome=request.technique, eng=request.eng)
        technique.put()

        return DefaultResponseMessage(message="Technique Added")

    @endpoints.method(ArtistRequestMessage, DefaultResponseMessage,
                      path='check', http_method='POST', name='check.checklogin')
    def check_login(self, request):
        blocked = BlackListEntry.query(BlackListEntry.email == request.email.lower()).get()

        if blocked is not None:
            return DefaultResponseMessage(message="Artist is Blocked!")

        artist = ArtistEntry.query(ArtistEntry.email == request.email.lower()).get()

        if artist is not None:
            return DefaultResponseMessage(message="Artist Registered!")
        else:
            return DefaultResponseMessage(message="Artist Not Found!")

    @endpoints.method(ArtistRequestMessage, PictureDetailsCollection,
                      path='artworks', http_method='GET', name='artworks.getartworks')
    def get_artworks(self, request):
        artist = ArtistEntry.query(ArtistEntry.email == request.email.lower()).get()

        if artist is None:
            return PictureDetailsCollection(message="Artist Not Found!")

        artworks = PictureEntry.query(ancestor=artist.key).order(-PictureEntry.date_time)

        messlist = []
        # AGGIUNGERE MESE E ANNO
        for pic in artworks:
            date_timeobj = pic.date_time
            date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
            technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
            messlist.append(PictureDetailsMessage(artist=pic.artist, title=pic.title, date_time=date_timestr,
                                                  url=pic.url, descr=pic.descr, luogo=pic.luogo, dim=pic.dim,
                                                  likes=pic.likes, technique=technique.nome,
                                                  anno=pic.anno, mese=pic.mese))

        return PictureDetailsCollection(photos=messlist)
    '''
    @endpoints.method(DownloadRequestMessage, DownloadResponseCollection,
                      path='display', http_method="GET", name="display.getphotos")
    def display_getphotos(self, request):
        fetch = request.fetch
        pictures = None

        if request.date_time is None and request.technique is None:
            pictures = PictureEntry.query().order(-PictureEntry.date_time).fetch(fetch)
        elif request.technique is None:
            date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            pictures = PictureEntry.query(PictureEntry.date_time < date_time).order(-PictureEntry.date_time).fetch(fetch)
        elif request.date_time is None:
            technique = TechniqueEntry.query(TechniqueEntry.nome == request.technique).get()
            if technique is not None:
                pictures = PictureEntry.query(PictureEntry.technique == technique.key).order(-PictureEntry.date_time).fetch(fetch)
        else:
            date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            technique = TechniqueEntry.query(TechniqueEntry.nome == request.technique).get()
            if technique is not None:
                pictures = PictureEntry.query(ndb.AND(PictureEntry.technique == technique.key,
                                                      PictureEntry.date_time < date_time)).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []
        if pictures is not None:
            for pic in pictures:
                date_timeobj = pic.date_time
                date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
                technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
                messlist.append(DownloadResponseMessage(artist=pic.artist, title=pic.title,
                                                        date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                        luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                        technique=technique.nome))

        return DownloadResponseCollection(photos=messlist)
    '''

    @endpoints.method(DownloadRequestMessage, DownloadResponseCollection,
                      path='display', http_method="GET", name="display.getphotos")
    def display_getphotos(self, request):
        fetch = request.fetch

        if request.date_time is None:
            pictures = PictureEntry.query().order(-PictureEntry.date_time).fetch(fetch)
        else:
            try:
                date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                return DownloadResponseCollection(message="Bad date found!")
            pictures = PictureEntry.query(PictureEntry.date_time < date_time).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []
        if pictures is not None:
            for pic in pictures:
                date_timeobj = pic.date_time
                date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
                technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
                if request.lang == 'ENG':
                    technique = technique.eng
                else:
                    technique = technique.nome
                # AGGIUNGERE MESE E ANNO
                messlist.append(DownloadResponseMessage(artist=pic.artist, title=pic.title,
                                                        date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                        luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                        technique=technique, anno=pic.anno, mese=pic.mese))

        return DownloadResponseCollection(photos=messlist)

    @endpoints.method(TechniqueSearchRequest, DownloadResponseCollection,
                      path='displaytechnique', http_method="GET", name="displaytechnique.getphotos")
    def displaytechnique_getphotos(self, request):
        fetch = request.fetch
        technique = TechniqueEntry.query(ndb.OR(TechniqueEntry.nome == request.technique,
                                                TechniqueEntry.eng == request.technique)).get()

        if technique is None:
            return DownloadResponseCollection(message="Technique not found!")

        if request.date_time is None:
            pictures = PictureEntry.query(PictureEntry.technique == technique.key).order(-PictureEntry.date_time).fetch(fetch)
        else:
            try:
                date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                return DownloadResponseCollection(message="Bad date found!")
            pictures = PictureEntry.query(ndb.AND(PictureEntry.technique == technique.key,
                                                  PictureEntry.date_time < date_time)).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []
        if pictures is not None:
            for pic in pictures:
                date_timeobj = pic.date_time
                date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
                technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
                if request.lang == 'ENG':
                    technique = technique.eng
                else:
                    technique = technique.nome
                # AGGIUNGERE MESE E ANNO
                messlist.append(DownloadResponseMessage(artist=pic.artist, title=pic.title,
                                                        date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                        luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                        technique=technique, anno=pic.anno, mese=pic.mese))

        return DownloadResponseCollection(photos=messlist)

    @endpoints.method(PlaceSearchRequest, DownloadResponseCollection,
                      path='displayplace', http_method="GET", name="displayplace.getphotos")
    def displayplaces_getphotos(self, request):
        fetch = request.fetch

        if request.date_time is None:
            pictures = PictureEntry.query(PictureEntry.luogo == request.luogo).order(-PictureEntry.date_time).fetch(fetch)
        else:
            try:
                date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                return DownloadResponseCollection(message="Bad date found!")
            pictures = PictureEntry.query(ndb.AND(PictureEntry.luogo == request.luogo,
                                                  PictureEntry.date_time < date_time)).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []
        if pictures is not None:
            for pic in pictures:
                date_timeobj = pic.date_time
                date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
                technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
                if request.lang == 'ENG':
                    technique = technique.eng
                else:
                    technique = technique.nome
                # AGGIUNGERE MESE E ANNO
                messlist.append(DownloadResponseMessage(artist=pic.artist, title=pic.title,
                                                        date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                        luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                        technique=technique, anno=pic.anno, mese=pic.mese))

        return DownloadResponseCollection(photos=messlist)

    @endpoints.method(RefreshRequestMessage, DownloadResponseCollection,
                      path='refresh', http_method="GET", name="refresh.refreshphotos")
    def refresh_photos(self, request):
        fetch = request.fetch
        pictures = None
        extra_second = timedelta(seconds=1)

        if request.technique is not None and request.luogo is not None:
            return DownloadResponseCollection(message="Both technique and luogo are not None!")

        if request.technique is None and request.luogo is None:
            try:
                date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                return DownloadResponseCollection(message="Bad date found!")
            query_date = date_time + extra_second
            pictures = PictureEntry.query(PictureEntry.date_time > query_date).order(-PictureEntry.date_time).fetch(fetch)
        elif request.technique is None:
            try:
                date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                return DownloadResponseCollection(message="Bad date found!")
            query_date = date_time + extra_second
            pictures = PictureEntry.query(ndb.AND(PictureEntry.luogo == request.luogo,
                                                  PictureEntry.date_time > query_date)).order(-PictureEntry.date_time).fetch(fetch)
        else:
            try:
                date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                return DownloadResponseCollection(message="Bad date found!")
            query_date = date_time + extra_second
            technique = TechniqueEntry.query(ndb.OR(TechniqueEntry.nome == request.technique,
                                                    TechniqueEntry.eng == request.technique)).get()
            if technique is not None:
                pictures = PictureEntry.query(ndb.AND(PictureEntry.technique == technique.key,
                                                      PictureEntry.date_time > query_date)).order(-PictureEntry.date_time).fetch(fetch)

        messlist = []

        for pic in pictures:
            date_timeobj = pic.date_time
            date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
            technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
            if request.lang == 'ENG':
                technique = technique.eng
            else:
                technique = technique.nome
            # AGGIUNGERE MESE E ANNO
            messlist.append(DownloadResponseMessage(artist=pic.artist, title=pic.title,
                                                    date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                    luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                    technique=technique, anno=pic.anno, mese=pic.mese))

        if len(messlist) == 0:
            return DownloadResponseCollection(message="No new artworks found!")

        return DownloadResponseCollection(photos=messlist)



    @endpoints.method(message_types.VoidMessage, ArtistBriefCollection,
                      path='artistlist', http_method="GET", name="artistlist.getartists")
    def artistlist_getartist(self, request):
        artists = ArtistEntry.query().order(ArtistEntry.cognome, ArtistEntry.nome)

        if artists is None:
            return ArtistBriefCollection(message="No artist found!")

        messlist = []

        for artist in artists:
            messlist.append(ArtistBriefMessage(email=artist.email, nome=artist.nome, cognome=artist.cognome,
                                               nickname=artist.nickname, pic=artist.pic))

        return ArtistBriefCollection(artists=messlist)

    @endpoints.method(InsertCommentMessage, DefaultResponseMessage,
                      path='comment', http_method='POST', name='comment.insertcomment')
    def insert_comment(self, request):
        if request.admin != "googleworkshop":
            blocked = BlackListEntry.query(BlackListEntry.email == request.email.lower()).get()

            if blocked is not None:
                return DefaultResponseMessage(message="Artist is Blocked!")

        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return DefaultResponseMessage(message="Artwork not found!")

        comment = CommentEntry(parent=photo.key, comment=request.comment, email=request.email.lower())
        key = comment.put()
        comment = key.get()
        comment.id = str(key.id())
        comment.put()

        return DefaultResponseMessage(message="Comment Added!!")

    @endpoints.method(DeleteCommentMessage, DefaultResponseMessage,
                      path='remove', http_method='GET', name='remove.removecomment')
    def remove_comment(self, request):
        comment = CommentEntry.query(CommentEntry.id == request.id).get()

        if comment is None:
            return DefaultResponseMessage(message="Comment not found!")

        comment.key.delete()

        return DefaultResponseMessage(message="Comment removed!")

    @endpoints.method(CommentListMessage, CommentDetailsCollection,
                      path='list', http_method='GET', name='list.commentlist')
    def comment_list(self, request):
        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return DefaultResponseMessage(message="Artwork not found!")

        if request.fetch is None:
            comments = CommentEntry.query(ancestor=photo.key).order(CommentEntry.date_time)
        else:
            comments = CommentEntry.query(ancestor=photo.key).order(CommentEntry.date_time).fetch(request.fetch)

        messlist = []

        if comments is None:
            return CommentDetailsCollection(message="No comments found!")

        extra_hour = timedelta(hours=1)

        for comment in comments:
            date_timeobj = comment.date_time + extra_hour
            date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
            artist = ArtistEntry.query(ArtistEntry.email == comment.email).get()
            if artist is not None:
                pic = artist.pic
                messlist.append(CommentDetails(comment=comment.comment, email=comment.email, id=comment.id,
                                               pic=pic, name="%s %s" % (artist.nome, artist.cognome), date=date_timestr))
            else:
                messlist.append(CommentDetails(comment=comment.comment, email=comment.email, id=comment.id, date=date_timestr))

        return CommentDetailsCollection(comments=messlist)

    @endpoints.method(GeneralLikeRequest, GeneralLikeResponse,
                      path='like', http_method='POST', name='like.likestatus')
    def like_status(self, request):
        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return GeneralLikeResponse(message="Artwork not found!")

        like = LikeEntry.query(ndb.AND(LikeEntry.url == request.url,
                                       LikeEntry.email == request.email.lower())).get()

        if like is None:
            return GeneralLikeResponse(hasLiked=False, likes=photo.likes)
        else:
            return GeneralLikeResponse(hasLiked=True, likes=photo.likes)

    @endpoints.method(GeneralLikeRequest, GeneralLikeResponse,
                      path='change', http_method='POST', name='change.changelikestatus')
    def change_like_status(self, request):
        if request.admin != "googleworkshop":
            blocked = BlackListEntry.query(BlackListEntry.email == request.email.lower()).get()

            if blocked is not None:
                return GeneralLikeResponse(message="Artist is Blocked!")

        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return GeneralLikeResponse(message="Artwork not found!")

        like = LikeEntry.query(ndb.AND(LikeEntry.url == request.url,
                                       LikeEntry.email == request.email.lower())).get()

        if like is None:  # Aggiungo il like
            photo.likes += 1
            photo.put()
            like = LikeEntry(parent=photo.key, url=request.url, email=request.email.lower())
            like.put()
            status = True
        else:  # Rimuovo il like
            if photo.likes >= 0:  # Per evitare errori strani
                photo.likes -= 1
            photo.put()
            like.key.delete()
            status = False

        return GeneralLikeResponse(hasLiked=status, likes=photo.likes)

    @endpoints.method(LikeListRequest, LikeDetailsCollection,
                      path='likeslist', http_method='GET', name='likeslist.getlikelist')
    def get_like_list(self, request):
        photo = PictureEntry.query(PictureEntry.url == request.url).get()

        if photo is None:
            return LikeDetailsCollection(message="Artwork not found!")

        likes = LikeEntry.query(LikeEntry.url == request.url)

        messlist = []

        for like in likes:
            artist = ArtistEntry.query(ArtistEntry.email == like.email).get()
            if artist is not None:
                messlist.append(LikeDetailsResponse(email=artist.email, name="%s %s" % (artist.nome, artist.cognome),
                                                    pic=artist.pic))
            else:
                messlist.append(LikeDetailsResponse(email=like.email))

        return LikeDetailsCollection(likes=messlist)

    @endpoints.method(LikedArtworksRequest, DownloadResponseCollection,
                      path='liked', http_method='GET', name='liked.likedartworks')
    def liked_artworks(self, request):
        if request.date_time is None:
            if request.fetch is None:
                likes = LikeEntry.query(LikeEntry.email == request.email).order(-LikeEntry.date_time)
            else:
                likes = LikeEntry.query(LikeEntry.email == request.email).order(-LikeEntry.date_time).fetch(request.fetch)
        else:
            try:
                date_time = datetime.strptime(request.date_time, "%Y-%m-%d %H:%M:%S")
            except ValueError:
                return DownloadResponseCollection(message="Bad date found!")
            if request.fetch is None:
                likes = LikeEntry.query(ndb.AND(LikeEntry.email == request.email,
                                                LikeEntry.date_time < date_time)).order(-LikeEntry.date_time)
            else:
                likes = LikeEntry.query(ndb.AND(LikeEntry.email == request.email,
                                                LikeEntry.date_time < date_time)).order(-LikeEntry.date_time).fetch(request.fetch)

        messlist = []

        for like in likes:
            logging.info("date: %s" % like.date_time.strftime("%Y-%m-%d %H:%M:%S"))
            pic = like.key.parent().get()
            if pic is not None:
                date_timeobj = pic.date_time
                date_timestr = date_timeobj.strftime("%Y-%m-%d %H:%M:%S")
                liked = like.date_time.strftime("%Y-%m-%d %H:%M:%S")
                technique = TechniqueEntry.query(TechniqueEntry.key == pic.technique).get()
                # AGGIUNGERE MESE E ANNO
                messlist.append(DownloadResponseMessage(liked=liked, artist=pic.artist, title=pic.title,
                                                        date_time=date_timestr, url=pic.url, descr=pic.descr,
                                                        luogo=pic.luogo, dim=pic.dim, likes=pic.likes,
                                                        technique=technique.nome, anno=pic.anno, mese=pic.mese))

        return DownloadResponseCollection(photos=messlist)

    @endpoints.method(LikeListRequest, DefaultResponseMessage,
                      path='likes', http_method='GET', name='likes.adddate')
    def add_date(self, request):
        current_user = endpoints.get_current_user()
        if current_user is None:
            raise endpoints.UnauthorizedException('Invalid token.')

        likes = LikeEntry.query(LikeEntry.url == request.url)

        for like in likes:
            if like.date_time is None:
                url = like.url
                email = like.email
                parent = like.key.parent()
                like.key.delete()
                new_like = LikeEntry(parent=parent, url=url, email=email)
                new_like.put()

        return DefaultResponseMessage(message="Dates Added")

    @endpoints.method(FlagRequestMessage, DefaultResponseMessage,
                      path='flag', http_method='GET', name='flag.flagcontent')
    def flag_content(self, request):
        blocked = BlackListEntry.query(BlackListEntry.email == request.flagged.lower()).get()

        if blocked is not None:
            return DefaultResponseMessage(message="Artist is Blocked!")

        artist_flagger = ArtistEntry.query(ArtistEntry.email == request.flagger.lower()).get()

        if artist_flagger is None:
            return DefaultResponseMessage(message="Flagger Not Found!")

        artist = ArtistEntry.query(ArtistEntry.email == request.flagged.lower()).get()

        '''
        if artist is None:
            return DefaultResponseMessage(message="Artist Not Found!")
        '''

        if request.code < 0 or request.code > 5:
            return DefaultResponseMessage(message="Bad Code Found!")

        # CONTROLLO SU CODICE E TIPO DI SEGNALAZIONE

        if request.comment is not None:  # Commento
            content = CommentEntry.query(CommentEntry.id == request.comment).get()
            if content is None:
                return DefaultResponseMessage(message="Comment Not Found!")
            artwork = PictureEntry.query(PictureEntry.url == request.url).get()
            if artwork is None:
                return DefaultResponseMessage(message="Artwork Not Found!")
            if content.email == artist_flagger.email:
                return DefaultResponseMessage(message="Bad Flagger Found!")
            content_user = "Commento: '%s', apposto sull'opera %s dell'artista %s" % (content.comment, artwork.url, artwork.artist)
            content_admin = content.id
            flag_entry = FlagEntry(flagger=request.flagger.lower(), flagged=request.flagged.lower(), content=content.id)
        else:  # Artwork
            content = PictureEntry.query(PictureEntry.url == request.url).get()
            if content is None:
                return DefaultResponseMessage(message="Artwork Not Found!")
            if content.artist == artist_flagger.email:
                return DefaultResponseMessage(message="Bad Flagger Found!")
            content_user = "Opera da te caricata: %s" % content.url
            content_admin = content.url
            flag_entry = FlagEntry(flagger=request.flagger.lower(), flagged=request.flagged.lower(), content=content.url)

        # CONTROLLO PER EVITARE SEGNALAZIONI DUPLICATE
        flag = FlagEntry.query(ndb.AND(FlagEntry.flagger == flag_entry.flagger,
                                       FlagEntry.content == flag_entry.content)).get()
        if flag is not None:
            return DefaultResponseMessage(message="Content Already Flagged!")

        # CONTROLLO PER EVITARE INCREMENTI DUPLICATI
        flag = FlagEntry.query(ndb.AND(FlagEntry.flagged == flag_entry.flagged,
                                       FlagEntry.content == flag_entry.content)).get()
        if flag is None and artist is not None:
            # INCREMENTO DEL CONTATORE DELLA GIUSTA QUANTITA'
            if request.code < 3:
                if artist.ban_counter is None:
                    artist.ban_counter = 1
                else:
                    artist.ban_counter += 1
            if artist.ban_counter is None:
                    artist.ban_counter = 2
            else:
                artist.ban_counter += 2
            artist.put()

        # INSERIMENTO DEL FLAG
        flag_entry.put()

        # INVIO MAIL DI SEGNALAZIONE
        message = mail.EmailMessage(sender="arteverywhere00@gmail.com",
                                    subject="Segnalazione dei contenuti")

        if artist is not None:
            message.to = '%s %s <%s>' % (artist.nome, artist.cognome, artist.email)
            nome_mail = artist.nome
        else:
            message.to = '<%s>' % request.flagged
            nome_mail = 'Visitatore'

        message.body = '''
        Caro %s,

        ti inviamo questa mail in quanto un contenuto da te aggiunto
        e' stato giudicato come inappropriato da un utente.
        Nello specifico: %s.
        Provvederemo a verificare la situazione segnalataci nel modo
        piu' obbiettivo possibile.
        Ci teniamo pero' a ricordarti che Art Everywhere e' stato concepito
        come un posto dove gli artisti e gli appassionati d'arte possano
        condividere ed apprezzare opere d'arte, in un modo aperto al dibattito e allo
        scambio di idee ma sempre nel rispetto degli altri utenti.
        A tale scopo non tolleriamo ripetute infrazioni delle linee guida e
        ripetute segnalazioni potrebbero causare il blocco permanente del
        tuo profilo.

        Il team di Art Everywhere.
        ''' % (nome_mail, content_user)

        message.html = '''
        <html>
            <head></head>
            <body>
                <p align="center"><img alt="" height="100" src="https://lh4.googleusercontent.com/-4EUhV6V6U9Y/VN24VjX-oDI/AAAAAAAAABw/I9ZVh-fYSXs/s798-no/ae.png" width="100"></p>
                <hr>
                <p>Caro %s,</p>
                <div>ti inviamo questa mail in quanto un contenuto da te aggiunto
                e' stato giudicato come inappropriato da un utente.</div>
                <div>Nello specifico: %s.</div>
                <div>Provvederemo a verificare la situazione segnalataci nel modo
                piu' obbiettivo possibile.</div>
                <div>Ci teniamo pero' a ricordarti che Art Everywhere e' stato concepito
                come un posto dove gli artisti e gli appassionati d'arte possano
                condividere ed apprezzare opere d'arte, in un modo aperto al dibattito e allo
                scambio di idee ma sempre nel rispetto degli altri utenti.</div>
                <div>A tale scopo non tolleriamo ripetute infrazioni delle linee guida e
                ripetute segnalazioni potrebbero causare il blocco permanente del
                tuo profilo.</div>

                <p>Il team di Art Everywhere.</p>
            </body>
        </html>
        ''' % (nome_mail, content_user)

        message.send()

        message = mail.EmailMessage(sender="arteverywhere00@gmail.com",
                                    subject="Segnalazione dei contenuti")

        message.to = '%s %s <%s>' % ("Art", "Everywhere", "arteverywhere00@gmail.com")

        message.body = '''
        E' arrivata una notifica di segnalazione di un contenuto ritenuto dall'
        utente %s inappropriato.
        Il contenuto e' stato caricato dall'utente %s ed e' il seguente: %s.
        ''' % (request.flagger, request.flagged, content_admin)

        message.html = '''
        <html>
            <head></head>
            <body>
                <p align="center"><img alt="" height="100" src="https://lh4.googleusercontent.com/-4EUhV6V6U9Y/VN24VjX-oDI/AAAAAAAAABw/I9ZVh-fYSXs/s798-no/ae.png" width="100"></p>
                <hr>
                <p>E' arrivata una notifica di segnalazione di un contenuto ritenuto dall'
                utente %s inappropriato.</p>
                <p>Il contenuto e' stato caricato dall'utente %s ed e' il seguente: %s.</p>>
            </body>
        </html>
        ''' % (request.flagger, request.flagged, content_admin)

        message.send()

        # CONTROLLO VIOLAZIONE DEI LIMITI
        if artist is None or artist.ban_counter > 7:
            blackmail = BlackListEntry(email=request.flagged)
            blackmail.put()

            message = mail.EmailMessage(sender="arteverywhere00@gmail.com",
                                        subject="Blocco del profilo")

            if artist is not None:
                message.to = '%s %s <%s>' % (artist.nome, artist.cognome, artist.email)
                nome_mail = artist.nome
            else:
                message.to = '<%s>' % request.flagged
                nome_mail = 'Visitatore'

            message.body = '''
            Caro %s,

            ti inviamo questa mail per notificarti il fatto che,
            a causa delle ripetute infrazioni delle linee guida di
            Art Everywhere, il tuo profilo e' stato bloccato.
            Durante il periodo di blocco non potrai loggarti e
            potrai utilizzare l'applicazione solamente in modalita' visitatore.
            Per ottenere lo sblocco del profilo, ti preghiamo di inviarci una mail,
            spiegandoci le tue motivazioni e la tua opinione sulle ripetute
            infrazioni da te commesse.
            Se per noi sarai idoneo il tuo profilo verra' riattivato e tu sarai
            di nuovo in grado di loggarti nell'app.

            Il team di Art Everywhere.
            ''' % nome_mail

            message.html = '''
            <html>
                <head></head>
                <body>
                    <p align="center"><img alt="" height="100" src="https://lh4.googleusercontent.com/-4EUhV6V6U9Y/VN24VjX-oDI/AAAAAAAAABw/I9ZVh-fYSXs/s798-no/ae.png" width="100"></p>
                    <hr>
                    <p>Caro %s,</p>
                    <div>ti inviamo questa mail per notificarti il fatto che,
                    a causa delle ripetute infrazioni delle linee guida di
                    Art Everywhere, il tuo profilo e' stato bloccato.</div>
                    <div>Durante il periodo di blocco non potrai loggarti e
                    potrai utilizzare l'applicazione solamente in modalita' visitatore.</div>
                    <div>Per ottenere lo sblocco del profilo, ti preghiamo di inviarci una mail,
                    spiegandoci le tue motivazioni e la tua opinione sulle ripetute
                    infrazioni da te commesse.</div>
                    <div>Se per noi sarai idoneo il tuo profilo verra' riattivato e tu sarai
                    di nuovo in grado di loggarti nell'app.</div>
                    <p>Il team di Art Everywhere.</p>
                </body>
            </html>
            ''' % nome_mail

            message.send()

        return DefaultResponseMessage(message="Content Flagged!")

    @endpoints.method(BlockedUserRequest, BlocekedUserResponse,
                      path='blocked', http_method='GET', name='blocked.isblocked')
    def is_blocked(self, request):
        user = BlackListEntry.query(BlackListEntry.email == request.email.lower()).get()

        if user is None:
            is_blocked = False
        else:
            is_blocked = True

        return BlocekedUserResponse(is_blocked=is_blocked)

    @endpoints.method(FlagListRequest, FlagEntryCollection,
                      path='flags', http_method='GET', name='flags.flaglist')
    def flag_list(self, request):
        flags = FlagEntry.query(FlagEntry.flagged == request.email.lower())

        messlist = []

        for flag in flags:
            messlist.append(FlagEntryMessage(flagger=flag.flagger, content=flag.content))

        return FlagEntryCollection(flags=messlist)
    '''
    @endpoints.method(FollowRequestMessage, DefaultResponseMessage,
                      path='follow', http_method='GET', name='follow.followartist')
    def follow_artist(self, request):
        follower = ArtistEntry.query(ArtistEntry.email == request.follower).get()
        if follower is None:
            return DefaultResponseMessage(message="Follower not found!")

        followed = ArtistEntry.query(ArtistEntry.email == request.followed).get()
        if followed is None:
            return DefaultResponseMessage(message="Followed not found!")

        follow = FollowEntry.query(ndb.AND(FollowEntry.follower == request.follower,
                                           FollowEntry.followed == request.followed)).get()
        if follow is not None:
            return DefaultResponseMessage(message="Artist already followed!")

        follow = FollowEntry(follower=request.follower, followed=request.followed)
        follow.put()

        return DefaultResponseMessage(message="Artist Followed!")

    @endpoints.method(FollowRequestMessage, DefaultResponseMessage,
                      path='unfollow', http_method='GET', name='unfollow.unfollowartist')
    def unfollow_artist(self, request):
        follow = FollowEntry.query(ndb.AND(FollowEntry.follower == request.follower,
                                           FollowEntry.followed == request.followed)).get()

        if follow is not None:
            follow.key.delete()
            return DefaultResponseMessage(message="Artist Unfollowed!")

        return DefaultResponseMessage(message="Artist not followed!")

    @endpoints.method(FollowedRequestMessage, ArtistBriefCollection,
                      path='followed', http_method='GET', name='followed.artistsfollowed')
    def artists_followed(self, request):
        followed = FollowEntry.query(FollowEntry.follower == request.follower)

        emails = []

        for follow in followed:
            emails.append(follow.followed)

        artists = ArtistEntry.query(ArtistEntry.email.IN(emails)).order(ArtistEntry.cognome, ArtistEntry.nome)
        messlist = []

        for artist in artists:
            messlist.append(ArtistBriefMessage(email=artist.email, nome=artist.nome, cognome=artist.cognome,
                                               nickname=artist.nickname, pic=artist.pic))

        return ArtistBriefCollection(artists=messlist)
    '''
    @classmethod
    def login(cls):
        gd_client = gdata.photos.service.PhotosService()
        gd_client.email = 'arteverywhere00@gmail.com'
        gd_client.password = 'googleworkshop'
        gd_client.source = 'art-everywhere'
        gd_client.ProgrammaticLogin()
        return gd_client

APPLICATION = endpoints.api_server([PicasaWA])