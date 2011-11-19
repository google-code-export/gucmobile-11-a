from django.conf.urls.defaults import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('backend.views',
    url(r'^api/(?P<lng>\d+\.\d+)/(?P<lat>\d+\.\d+)/(?P<who>[\w\-_]+)$', 'api'),
    url(r'^test/(?P<test_value>(.)+)', 'test_method_in_views'),
<<<<<<< HEAD
   # url(r'^test_evaluate/', 'route_blockage'),
    url(r'^test_evaluate/(?P<origin>(.)+)/(?P<destination>(.)+)/([a-z]+)/([a-z]+)/$', 'route_blockage'),
    url(r'^directions/(?P<origin>(.)+)/(?P<destination>(.)+)/([a-z]+)/([a-z]+)/$', 'directions'),
=======
    url(r'^test_evaluate/', 'route_blockage'),
    url(r'^directions/(?P<origin>(.)+)/(?P<destination>(.)+)/([a-z]+)/$', 'directions'),
>>>>>>> e3f08032351977a7909f229bc90bcfa55ca2545e
    url(r'^alternatives/(?P<location>\d+)/(?P<destination>\d+)$', 'alternatives'),
    url(r'^admin/', include(admin.site.urls)),
)
