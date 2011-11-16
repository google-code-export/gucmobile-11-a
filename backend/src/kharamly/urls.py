from django.conf.urls.defaults import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('backend.views',
     url(r'^getdirections/(?P<origin>[\w\-]+)/(?P<destination>[\w\-]+)/(?P<sensor>[a-z]+)/(?P<alternatives>[a-z]+)/$', 'getdirections'),
     url(r'^api/(?P<lng>\d+\.\d+)/(?P<lat>\d+\.\d+)/(?P<who>[\w\-_]+)$', 'api'),
     url(r'^getalternative/(?P<location>\d+)/(?P<destination>\d+)$', 'getalternative'),
     url(r'^admin/', include(admin.site.urls)),
)
